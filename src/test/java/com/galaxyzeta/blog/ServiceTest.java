package com.galaxyzeta.blog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.Comment;
import com.galaxyzeta.blog.entity.Role;
import com.galaxyzeta.blog.entity.Tag;
import com.galaxyzeta.blog.entity.User;
import com.galaxyzeta.blog.entity.UserLike;
import com.galaxyzeta.blog.exceptions.AccessViolationException;
import com.galaxyzeta.blog.exceptions.CustomException;
import com.galaxyzeta.blog.service.CommentService;
import com.galaxyzeta.blog.service.RoleService;
import com.galaxyzeta.blog.service.TagService;
import com.galaxyzeta.blog.service.BlogService;
import com.galaxyzeta.blog.service.UserService;
import com.galaxyzeta.blog.util.Constants;
import com.galaxyzeta.blog.util.Pager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ServiceTest {

	@Autowired
	UserService userService;

	@Autowired
	BlogService blogService;

	@Autowired
	TagService tagService;

	@Autowired
	CommentService commentService;

	@Autowired
	RoleService roleService;

	@Autowired
	StringRedisTemplate redisTemplate;
	
	@BeforeEach
	void beforeLoad() {
		// 默认的两个角色
		roleService.saveRole(new Role(Constants.ROLE_ADMIN));
		roleService.saveRole(new Role(Constants.ROLE_USER));
		// 清空 redis
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	/** 产生一个默认的测试用户 */
	Integer registerMockUser() {
		User user = new User("asd", "Valid123", "123@qq.com");
		userService.requestCaptcha(user.getMail());
		user.setCaptcha(userService.getCaptcha(user.getMail()));
		return userService.register(user).getUid();
	}

	/** 生成一个自定义的用户 */
	Integer registerMockUser(String name, String password, String mail) {
		User user = new User(name, password, mail);
		userService.requestCaptcha(user.getMail());
		user.setCaptcha(userService.getCaptcha(user.getMail()));
		return userService.register(user).getUid();
	}

	/** 生成多个随机用户 */
	List<Integer> registerMockUsers(int count) {
		Integer[] arr = new Integer[count];
		for(int i=0; i<count; i++) {
			arr[i] = registerMockUser("user"+i, "Valid123", "user"+i+"@galaxyzeta.com");
		}
		List<Integer> res = new ArrayList<>();
		Collections.addAll(res, arr);
		return res;
	}

	/** 生成随机博客文章，用户随机选择 */
	List<Integer> mockBlogData(List<Integer> userList, int count) throws Exception {
		int len = userList.size();
		List<Integer> ids = new LinkedList<>();
		for(int i=0; i<count; i++) {
			int rndUid = userList.get((int)(Math.random() * len));
			Blog blog = new Blog(rndUid, "blog"+i, "blog"+i);
			blogService.saveBlog(blog);
			ids.add(blog.getBid());
			// 模拟插入延迟
			Thread.sleep(10);
		}
		return ids;
	}

	/** 随机选取用户给博客点赞 */
	void mockBlogLike(List<Integer> userList, Integer blogId) throws Exception {
		int len = userList.size();
		int rndUid = userList.get((int)(Math.random() * len));
		blogService.preferBlog(new UserLike(rndUid, blogId));
	}

	@Test
	@Transactional
	void registerLoginTest() {
		userService.deleteByMail("asd");
		User user = new User("asd", "Valid123", "123@qq.com");
		// 未注册用户测试
		assertThrows(CustomException.class, ()->userService.login("asd", "Valid123"));
		// 错误验证码测试
		assertThrows(AccessViolationException.class, ()->userService.register(user));
		// 正确验证码测试
		userService.requestCaptcha(user.getMail());
		user.setCaptcha(userService.getCaptcha(user.getMail()));
		assertDoesNotThrow(()->userService.register(user));
		// 重复注册
		assertThrows(CustomException.class, ()->userService.register(user));
		// 登录密码错误
		assertThrows(CustomException.class, ()->userService.login("123@qq.com", "wrong"));
		// 正确的登录
		assertDoesNotThrow(()->userService.login("123@qq.com", "Valid123"));
		// 连续失败登录
		int flag = 0;
		for(int i=0; i<10; i++) {
			try {
				userService.login("123@qq.com", "wrong");			
			} catch (AccessViolationException e) {
				System.out.println(e.getMessage());
				flag = 1;
			}
		}
		assertEquals(1, flag);
	}

	@Test
	@Transactional
	void commentCrudTest() throws Exception {
		// 准备测试用户
		Integer uid = registerMockUser();
		// 准备标签
		List<Tag> tags = new ArrayList<>();
		tags.add(new Tag(uid, "java"));
		tags.add(new Tag(uid, "C#"));
		Blog newBlog = new Blog(uid, "QQQ", "content", tags);
		blogService.saveBlog(newBlog);
		// 准备评论
		List<Comment> toBeInserted = new ArrayList<>();
		toBeInserted.add(new Comment("VALID", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), null));
		toBeInserted.add(new Comment("VALID", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), null));
		toBeInserted.add(new Comment("VALID", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), null));
		// 插入评论
		for (Comment c : toBeInserted) {
			commentService.saveComment(c);
		}
		assertNotNull(toBeInserted.get(0).getCid());
		// 根据blogId分页获取
		List<Comment> list = commentService.getByBlogIdPaged(newBlog.getBid(), new Pager(1, 10));
		assertEquals(toBeInserted.size(), list.size());
		// 根据用户名分页获取
		list = commentService.getByUserIdPaged(uid, new Pager(1, 10));
		assertEquals(toBeInserted.size(), list.size());
		// 删除评论
		commentService.userDeleteByCommentId(list.get(0).getCid(), list.get(0).getUid());
		assertNull(commentService.getByCommentId(list.get(0).getCid()));
		// 增加点赞
		commentService.preferIncrement(list.get(1).getCid(), 1);
		Comment c = commentService.getByCommentId(list.get(1).getCid());
		assertEquals(1, c.getPrefer());
	}

	@Transactional
	@Test
	void commentCrudTest2() throws Exception {
		
		// == 测试准备 ==

		// 准备测试用户
		Integer uid = registerMockUser();
		// 准备标签
		List<Tag> tags = new ArrayList<>();
		tags.add(new Tag(uid, "java"));
		tags.add(new Tag(uid, "C#"));
		Blog newBlog = new Blog(uid, "QQQ", "content", tags);
		blogService.saveBlog(newBlog);
		
		// 准备多级评论
		Comment c1 = new Comment("VALID 1", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), null);
		commentService.saveComment(c1);

		Comment c1_1 = new Comment("VALID 1-1", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), c1.getCid());
		commentService.saveComment(c1_1);

		Comment c1_2 = new Comment("VALID 1-2", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), c1.getCid());
		commentService.saveComment(c1_2);

		Comment c1_2_1 = new Comment("VALID 1-2-1", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), c1_2.getCid());
		commentService.saveComment(c1_2_1);

		Comment c2 = new Comment("VALID 2", userService.getUserByMail("123@qq.com").getUid(), newBlog.getBid(), null);
		commentService.saveComment(c2);
		
		// == 测试开始 == 
		c1 = commentService.getByCommentId(c1.getCid());
		assertEquals(5, commentService.getCommentCountByBlogId(newBlog.getBid()));
		assertEquals(4, commentService.getCommentsByRootParentId(c1.getCid()).size());
		
		commentService.adminDeleteByCommentId(c1_2.getCid());
		assertEquals(3, commentService.getCommentCountByBlogId(newBlog.getBid()));
		assertEquals(2, commentService.getCommentsByRootParentId(c1.getCid()).size());

		c2 = commentService.getByCommentId(c2.getCid());
		commentService.adminDeleteByCommentId(c2.getCid());
		assertEquals(2, commentService.getCommentCountByBlogId(newBlog.getBid()));
		assertEquals(0, commentService.getCommentsByRootParentId(c2.getCid()).size());

		commentService.adminDeleteByCommentId(c1.getCid());
		assertEquals(0, commentService.getCommentCountByBlogId(newBlog.getBid()));

	}
	
	@Test
	@Transactional
	void blogCrudTest() throws Exception {
		// 准备测试用户
		Integer uid = registerMockUser();
		System.out.println(uid);
		// 准备标签
		List<Tag> tags = new ArrayList<>();
		tags.add(new Tag(uid, "java"));
		tags.add(new Tag(uid, "C#"));
		
		// === 阶段 1 ===

		// 检查标签是否跟随博文插入		
		Blog newBlog = new Blog(uid, "QQQ", "content", tags);
		blogService.saveBlog(newBlog);
		assertEquals(true, tagService.existsByName("C#"));
		assertNotNull(blogService.getBlogById(newBlog.getBid()));
		// 检查标签是否重复插入
		Blog newBlog2 = new Blog(uid, "DDD", "content", tags);
		blogService.saveBlog(newBlog2);
		assertEquals(2, tagService.getAllTagsPaged(new Pager(1, 10)).size());
		// 检查标签更新
		newBlog.setTitle("BBB");
		blogService.update(newBlog);
		assertEquals("BBB", blogService.getBlogById(newBlog.getBid()).getTitle());
		// 检查博客数量查询
		assertEquals(2, blogService.getBlogCountByUserId(uid));
		// 各项数值的加减操作
		/*
		blogService.commentIncrement(newBlog.getBid(), 1);
		blogService.viewedIncrement(newBlog.getBid(), 1);
		blogService.preferIncrement(newBlog.getBid(), 1);
		blogService.commentIncrement(newBlog.getBid(), -1);
		blogService.viewedIncrement(newBlog.getBid(), -1);
		blogService.preferIncrement(newBlog.getBid(), -1);
		newBlog = blogService.getBlogById(newBlog.getBid());
		System.out.println(newBlog.getPrefer());
		assertEquals(0, newBlog.getComment());
		assertEquals(0, newBlog.getViewed());
		assertEquals(0, newBlog.getPrefer());
		*/
		// 删除测试
		blogService.deleteByBlogId(newBlog.getBid(), newBlog.getUid());
		assertNull(blogService.getBlogById(newBlog.getBid()));
		assertEquals(0, blogService.getTagsByBlogId(newBlog.getBid()).size());
		// 删除不存在数据
		assertThrows(AccessViolationException.class, ()->blogService.deleteByBlogId(Integer.MAX_VALUE, newBlog.getUid()));
	}

	@Test
	@Transactional
	void blogCrudTest2() throws Exception {
		// === 阶段 2 ===
		// 生成一组随机用户和一组随机文章
		List<Integer> userList = registerMockUsers(10);
		List<Integer> blogIds = mockBlogData(userList, 50);

		// 获取指定 blog
		System.out.println(blogService.getBlogById(blogIds.get(0)));

		// 获取主页，所有数据都在缓存中
		List<Blog> blogList = blogService.getHomeBlogs(new Pager(1, 10));
		System.out.println(blogList);
		assertEquals(10, blogList.size());

		// 查询的数据不在缓存中
		blogList = blogService.getHomeBlogs(new Pager(4, 10));
		System.out.println(blogList);
		assertEquals(10, blogList.size());

		// 查询的数据部分在缓存中
		blogList = blogService.getHomeBlogs(new Pager(2, 20));
		System.out.println(blogList);
		assertEquals(20, blogList.size());

		// 博客点赞
		blogService.preferBlog(new UserLike(0, blogList.get(0).getBid()));
		final List<Blog> temp = blogList;
		// 重复点赞
		assertThrows(AccessViolationException.class, ()->blogService.preferBlog(new UserLike(0, temp.get(0).getBid())));
		assertEquals(1, blogService.getPreferByBlogId(temp.get(0).getBid()));

		// 查询热门博客
		blogList = blogService.getHotBlogs(new Pager(1, 10));
		System.out.println(blogList);

	}

	@Test
	@Transactional
	void blogKeywordSearchTest() throws Exception {
		// 全文搜索测试
		Integer uid = registerMockUser();
		blogService.saveBlog(new Blog(uid, "Java is the best language ever!", "Java is my favourite language. Java is so awesome"));
		blogService.saveBlog(new Blog(uid, "Why C# is great", "C# is great, don't waste time programming too much with Java."));
		blogService.saveBlog(new Blog(uid, "Life is short, use python instead", "Use python and save your time"));
		blogService.saveBlog(new Blog(uid, "Web MVC design", "I use java and spring boot designing a useful web application"));
		List<Blog> blogs = blogService.getHomeBlogs(new Pager(1, 10));
		System.out.println(blogs);
		// 全文搜索测试，除非去掉 Transational 不然不会出现查找结果
		System.out.println(blogService.searchBlogPaged("java", new Pager(1, 10)));
	}

	@Test
	@Transactional
	void tagCrudTest() {
		// 准备测试用户 
		Integer uid = registerMockUser();
		// 准备标签
		Tag tag = new Tag(uid, "java");
		// 插入标签
		tagService.insert(tag);
		System.out.println(tag.getTid());
		assertNotNull(tagService.getByTagId(tag.getTid()));
		// 更新标签
		tag.setName("C#");
		tagService.update(tag);
		assertEquals("C#", tagService.getByTagId(tag.getTid()).getName());
		// 删除标签
		tagService.deleteByTagId(tag.getTid());
		assertNull(tagService.getByTagId(tag.getTid()));
	}

	
	@Test
	@Transactional
	void roleCrudTest() {
		Integer uid = registerMockUser();
		// 授予不存在的权限
		assertThrows(AccessViolationException.class, ()->roleService.revokeUserRole(uid, "ASD"));
		// 数据库存入权限
		roleService.saveRole(new Role("VISITOR"));
		assertNotNull(roleService.getRoleByRoleName("VISITOR"));
		// 授权
		roleService.grantUserRole(uid, "VISITOR");
		// 重复授权
		roleService.grantUserRole(uid, "VISITOR");
		// 权限判断
		assertEquals(false, roleService.hasRole(uid, "NO_ROLE"));
		assertEquals(true, roleService.hasRole(uid, "VISITOR"));
		// 移除不存在的权限，会抛出异常
		assertThrows(AccessViolationException.class, ()->roleService.revokeUserRole(uid, "ASD"));
		// 移除存在的权限
		assertDoesNotThrow(()->roleService.revokeUserRole(uid, "VISITOR"));
		assertEquals(false, roleService.hasRole(uid, "VISITOR"));
	}

}
