package com.galaxyzeta.blog.controller;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;

import com.galaxyzeta.blog.entity.Tag;
import com.galaxyzeta.blog.service.TagService;
import com.galaxyzeta.blog.util.Pager;
import com.galaxyzeta.blog.util.Result;
import com.galaxyzeta.blog.util.ResultFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/tag")
public class TagController {

	@Autowired
	private TagService tagService;

	@GetMapping(value="/")
	public Result getAllPaged(@Min(0) @PathVariable Integer currentPage, @Min(0) @PathVariable int itemPerPage) {
		return ResultFactory.getSuccess("查询成功", tagService.getAllTagsPaged(new Pager(currentPage, itemPerPage)));
	}

	@PostMapping(value="/{cid}")
	public Result insert(@PathVariable int uid, @RequestBody String tagname) {
		tagService.insert(new Tag(uid, tagname));
		return ResultFactory.getSuccess("增加成功", null);
	}

	@PutMapping(value="/{id}")
	public Result update(@PathVariable Integer id, @RequestBody Tag tag) {
		tagService.update(tag);
		return ResultFactory.getSuccess("修改成功", null);
	}
	
	@DeleteMapping(value = "/{id}")
	public Result delete(@PathVariable Integer id) {
		tagService.deleteByTagId(id);
		return ResultFactory.getSuccess("删除成功", null);
	}
}
