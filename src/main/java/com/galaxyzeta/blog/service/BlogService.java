package com.galaxyzeta.blog.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.galaxyzeta.blog.dao.BlogTagRefDao;
import com.galaxyzeta.blog.dao.TagDao;
import com.galaxyzeta.blog.dao.UserLikeDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxyzeta.blog.dao.BlogDao;
import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.BlogLikeParam;
import com.galaxyzeta.blog.entity.BlogTagRef;
import com.galaxyzeta.blog.entity.Tag;
import com.galaxyzeta.blog.entity.UserLike;
import com.galaxyzeta.blog.exceptions.AccessViolationException;
import com.galaxyzeta.blog.util.BloomFilterUtil;
import com.galaxyzeta.blog.util.Constants;
import com.galaxyzeta.blog.util.FormatUtil;
import com.galaxyzeta.blog.util.Pager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogService {

	@Autowired
	private BlogDao blogDao;

	@Autowired
	private BlogTagRefDao blog2tagDao;

	@Autowired
	private TagDao tagDao;

	@Autowired
	private UserLikeDao userLikeDao;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private RedisService redisService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BloomFilterUtil bloomFilterUtil;

	@Transactional
	/** 保存 blog 具体信息，更新最新博客缓存 */
	public void saveBlog(Blog blog) throws JsonProcessingException {
		
		blog.setCreatedAt(new Date());
		blog.setUpdatedAt(new Date());
		blog.setStatus(0);
		List<Tag> tags = blog.getTags();

		if(tags != null) {
			// 保存博客标签
			tagDao.saveManyTags(tags);
		}
		
		blogDao.saveBlog(blog);

		if(tags != null) {
			// 存入数据库后得到 bid，根据 bid - tagid 保存关联标签
			List<BlogTagRef> refs = getBlogTagRefs(blog.getTags(), blog.getBid());
			blog2tagDao.saveManyRefs(refs);
		}

		// 插入 new blog 缓存
		// redis blog_new: (Left push -->) FRESH <---------> STALE (Right pop-->)
		redisTemplate.opsForList().leftPush(Constants.REDIS_NEW_BLOG, Integer.toString(blog.getBid()));
		redisService.saveBlogToRedis(blog);
		// 插入bloomFilter
		bloomFilterUtil.save(Constants.REDIS_BLOOM_FILTER_BLOG, String.valueOf(blog.getBid()));
		// 如果容量超过最大，移除最左端 new blog
		if(redisTemplate.opsForList().size(Constants.REDIS_NEW_BLOG) > Constants.REDIS_NEW_BLOG_MAX_COUNT) {
			redisTemplate.opsForList().rightPop(Constants.REDIS_NEW_BLOG);
		}
	}

	/** 根据 id 查询 blog 详细信息，不更新缓存 */
	public Blog getBlogById(Integer blogId) throws IOException {
		
		// 查询布隆过滤器
		if(! bloomFilterUtil.exists(Constants.REDIS_BLOOM_FILTER_BLOG, String.valueOf(blogId))) {
			return null;
		}

		if(redisTemplate.hasKey(Constants.REDIS_BLOG_PREFIX + blogId)) {
			return redisService.getBlog(blogId);
		} else {
			Blog blog = blogDao.getByBlogId(blogId);
			if(blog == null) return null;
			
			blog.setTags(tagDao.getTagsByBlogId(blogId));
			return blog;
		}

	}

	/** 根据用户 id 获取 blog 摘要列表，不更新缓存 */
	public List<Blog> getByUserIdPaged(Integer uid, Pager pager) {
		List<Blog> res = blogDao.getByUserIdPaged(uid, pager);
		for(Blog blog : res) {
			blog.setTags(tagDao.getTagsByBlogId(blog.getBid()));
		}
		return res;
	}

	/** 获得热门博客的摘要列表 */
	public List<Blog> getHotBlogs(Pager pager) throws IOException {
		List<Blog> res = null;
		if(! redisTemplate.hasKey(Constants.REDIS_HOT_BLOG)) {
			// 没有缓存
			return updateHotBlogIntoRedis();
		}
		// 有缓存
		List<String> li = redisTemplate.opsForList().range(Constants.REDIS_HOT_BLOG, 0, Constants.REDIS_HOT_BLOG_MAX_COUNT);
		res = new ArrayList<>();
		for(String blogJson: li) {
			res.add(objectMapper.readValue(blogJson, Blog.class));
		}
		return res;
	}

	/** 更新热门博客，获得博客文章的摘要列表 */
	public List<Blog> updateHotBlogIntoRedis() throws JsonProcessingException, IOException {

		transferLikeFromRedis2DB();

		List<Blog> blogList = blogDao.getHotBlogs(0, Constants.REDIS_HOT_BLOG_MAX_COUNT);
		for(Blog blog: blogList) {
			redisService.saveBlogToRedis(blog);
		}

		return blogList;
	}

	/** 获得主页博客的摘要列表（按时间降序排列） */
	public List<Blog> getHomeBlogs(Pager pager) throws IOException {
		if(redisTemplate.hasKey(Constants.REDIS_NEW_BLOG)) {
			// 有缓存
			if(pager.getItemStart() >= Constants.REDIS_NEW_BLOG_MAX_COUNT) {
				// 1. 查找目标在缓存外，查数据库
				return blogDao.getHomeBlogs(pager);
			} else if (pager.getItemStart() + pager.getItemPerPage() > Constants.REDIS_NEW_BLOG_MAX_COUNT) {
				// 2. 查找目标部分在缓存中
				List<Blog> res = blogDao.getHomeBlogsByParams(Constants.REDIS_NEW_BLOG_MAX_COUNT, Constants.REDIS_NEW_BLOG_MAX_COUNT - pager.getItemStart());
				List<String> cacheList = redisTemplate.opsForList().range(Constants.REDIS_NEW_BLOG, pager.getItemStart(), Constants.REDIS_NEW_BLOG_MAX_COUNT);
				for(String blogId: cacheList) {
					Blog blog = redisService.getBlog(Integer.valueOf(blogId));
					// 截取部分内容作为摘要
					blog.setContent(FormatUtil.trimContent(blog.getContent(), Constants.BLOG_BODY_OVERVIEW_LIMIT));
					res.add(blog);
				}
				return res;
			} else {
				// 3. 查找目标全在缓存中
				List<Blog> res = new LinkedList<>();
				List<String> cacheList = redisTemplate.opsForList().range(Constants.REDIS_NEW_BLOG, pager.getItemStart(), pager.getItemStart() + pager.getItemPerPage() -1);
				for(String blogId: cacheList) {
					Blog blog = redisService.getBlog(Integer.valueOf(blogId));
					// 截取部分内容作为摘要
					blog.setContent(FormatUtil.trimContent(blog.getContent(), Constants.BLOG_BODY_OVERVIEW_LIMIT));
					res.add(blog);
				}
				return res;
			}
		} else {
			// 没缓存
			return blogDao.getHomeBlogs(pager);
		}
	}

	/** 根据博客 id 删除博客 */
	public void deleteByBlogId(Integer blogId, Integer userId) {
		// 验证确实是本人操作
		final Blog dbBlog = blogDao.getByBlogId(blogId);
		if(dbBlog == null) throw new AccessViolationException("待删除数据不存在");
		if(!dbBlog.getUid().equals(userId)) throw new AccessViolationException("无权操作");

		blogDao.deleteByBlogId(blogId);
		// 级联删除博客关联的标签，使用外键实现
		// blog2tagDao.deleteTagByBlogId(blogId);
		// 缓存清除
		redisTemplate.delete(Constants.REDIS_BLOG_PREFIX + blogId);
	}
	
	/** 更新博客 */
	public void update(Blog blog) throws JsonProcessingException {
		// 验证本人操作
		final Blog dbBlog = blogDao.getByBlogId(blog.getBid());
		if(dbBlog == null) throw new AccessViolationException("待删除数据不存在");
		if(!dbBlog.getUid().equals(blog.getUid())) throw new AccessViolationException("无权操作");
		// 日期更新
		blog.setUpdatedAt(new Date());
		// 更新数据库
		blogDao.update(blog);
		// 保存可能新增的标签
		tagDao.saveManyTags(blog.getTags());
		// 移除关联的 blogid-tagid
		blog2tagDao.deleteTagByBlogId(blog.getBid());
		// 重新关联 blogid-tagid
		List<BlogTagRef> refs = getBlogTagRefs(blog.getTags(), blog.getBid());
		blog2tagDao.saveManyRefs(refs);
		// 重新设置缓存
		if(redisTemplate.hasKey(Constants.REDIS_BLOG_PREFIX + blog.getBid())) {
			redisTemplate.opsForValue().set(Constants.REDIS_BLOG_PREFIX + blog.getBid(), objectMapper.writeValueAsString(blog));
		}
	}

	/** 获得博客和博客标签的关联对象 */
	private List<BlogTagRef> getBlogTagRefs(List<Tag> tags, Integer blogId) {
		List<BlogTagRef> refs = new LinkedList<>();
		for(Tag tag : tags) {
			refs.add(new BlogTagRef(blogId, tag.getTid()));
		}
		return refs;
	}

	/** 用户点赞 key 的生成 */
	private String getUserLikeKey(UserLike likeObject) {
		StringBuilder sb = new StringBuilder();
		sb.append(likeObject.getUid());
		sb.append(Constants.REDIS_LIKE_KEY_MIDDLE);
		sb.append(likeObject.getBid());
		return sb.toString();
	}
	
	/** 根据博客 id 获取具体标签 */
	public List<Tag> getTagsByBlogId(Integer blogId) {
		return tagDao.getTagsByBlogId(blogId);
	}

	/** 给博客文章点赞/取消点赞 */
	public void preferBlog(UserLike likeObject) throws JsonProcessingException, IOException {
		String userLikeKey = getUserLikeKey(likeObject);
		int val = likeObject.getStatus() == 1 ? 1 : -1;
		
		UserLike effectiveLikedObject = null;
		boolean isNew = false;

		// 查看缓存中是否有点赞信息
		UserLike cachedLikeObject = null;
		
		if(redisTemplate.opsForHash().hasKey(Constants.REDIS_USER_BLOG_LIKE_HASH, userLikeKey)) {
			cachedLikeObject = objectMapper.readValue(
				(String)redisTemplate.opsForHash().get(Constants.REDIS_USER_BLOG_LIKE_HASH, userLikeKey), UserLike.class);
		}
		
		// 获得要操作的目标
		if(cachedLikeObject == null) {
			// 没有点赞信息缓存，从数据库找
			effectiveLikedObject = userLikeDao.getUserLike(likeObject);
			if(effectiveLikedObject == null) {
				// 数据库中没有，这是一个新的点赞请求
				effectiveLikedObject = likeObject;
				isNew = true;
			}
		} else {
			// 从缓存中得到
			effectiveLikedObject = cachedLikeObject;
		}

		if(!isNew && effectiveLikedObject.getStatus() == val) {
			throw new AccessViolationException("重复操作"); // 重复操作，无效
		}	
		// 用户点赞数据写入缓存
		redisTemplate.opsForHash().put(Constants.REDIS_USER_BLOG_LIKE_HASH, userLikeKey, objectMapper.writeValueAsString(likeObject));

		// 博客赞数量写入缓存
		int count = isNew? effectiveLikedObject.getStatus(): effectiveLikedObject.getStatus() + val;
		redisTemplate.opsForHash().put(Constants.REDIS_BLOG_LIKE_COUNT_HASH, String.valueOf(effectiveLikedObject.getBid()), 
			String.valueOf(count));
		
	}

	/** 获得当前某博客点赞数 */
	public Integer getPreferByBlogId(Integer bid) {
		Integer count = Integer.valueOf((String)redisTemplate.opsForHash().get(Constants.REDIS_BLOG_LIKE_COUNT_HASH, String.valueOf(bid)));
		if(count == null) {
			// 查数据库
			return blogDao.getPreferByBlogId(bid);
		}
		return count;
	}

	/** 点赞从 redis 转移到 DB 
	 *  定时任务
	*/
	public void transferLikeFromRedis2DB() throws IOException {
		// 转移用户点赞信息
		List<UserLike> li = redisService.rmUserLikeFromRedis();
		userLikeDao.saveManyUserLikes(li);
		// 转移博客文章点赞数量
		List<BlogLikeParam> li2 = redisService.rmBlogLikeCountFromRedis();
		blogDao.updateBlogPrefersByBlogParams(li2);
	}

	/** 获得某用户的博客数量 */
	public Integer getBlogCountByUserId(Integer uid) {
		return blogDao.getBlogCountByUserId(uid);
	}

	/** 获得所有博客数量 */
	public Integer getAllBlogCount() {
		return blogDao.getAllBlogCount();
	}

	/** 搜索博客文章关键词，得到文章摘要列表 */
	public List<Blog> searchBlogPaged(String keyword, Pager pager) {
		return blogDao.searchBlogPagedBySQL(keyword, pager);
	}
}
