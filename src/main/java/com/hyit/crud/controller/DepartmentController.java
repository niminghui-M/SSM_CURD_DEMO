package com.hyit.crud.controller;

import com.hyit.crud.bean.Department;
import com.hyit.crud.bean.Msg;
import com.hyit.crud.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 处理和部门有关的请求
 */
@Controller
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @RequestMapping("/departments")
    @ResponseBody
    public Msg getDepartments() {
        //查出的所有部门信息
        List<Department> list = departmentService.getDepartments();
        return Msg.success().add("departments",list);
    }

}
