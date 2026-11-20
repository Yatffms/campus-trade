package com.campus.trade.VO;


import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;//当前页数据
    private Long total;//总条数
    private Long size;//每页大小
    private Long page;//当前页
    private Long pages;//总页数
}
