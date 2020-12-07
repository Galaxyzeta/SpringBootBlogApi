package com.galaxyzeta.blog.service;

import com.galaxyzeta.blog.dao.TagDao;
import com.galaxyzeta.blog.entity.Tag;
import com.galaxyzeta.blog.util.Pager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {

	@Autowired
	private TagDao tagDao;

	
	public void insert(Tag tag) {
		tagDao.saveTag(tag);
	}

	
	public void deleteByTagId(Integer tid) {
		tagDao.deleteByTagId(tid);
	}

	
	public Tag getByTagId(Integer tid) {
		return tagDao.getByTagId(tid);
	}

	
	public void update(Tag tag) {
		tagDao.update(tag);
	}

	
	public boolean existsByName(String name) {
		return tagDao.getByName(name) != null;
	}
	
	public List<Tag> getAllTagsPaged(Pager pager) {
		return tagDao.getAllTagsPaged(pager);
	}
	
}
