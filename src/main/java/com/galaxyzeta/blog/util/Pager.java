package com.galaxyzeta.blog.util;

import javax.validation.constraints.Min;

// Immutable
public class Pager {

	@Min(0)
	private int currentPage;
	@Min(0)
	private int itemPerPage;
	@Min(0)
	private int itemStart;

	public Pager(int currentPage, int itemPerPage) {
		this.currentPage = currentPage;
		this.itemPerPage = itemPerPage;
		itemStart = (currentPage - 1) * itemPerPage;
	}

	public final int getCurrentPage() {
		return currentPage;
	}
	public final int getItemPerPage() {
		return itemPerPage;
	}
	public final int getItemStart() {
		return itemStart;
	}
}
