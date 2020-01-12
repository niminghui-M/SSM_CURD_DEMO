package com.hyit.crud.service;

import com.hyit.crud.bean.Department;
import com.hyit.crud.dao.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Department> getDepartments() {
        return departmentMapper.selectByExample(null);
    }

}
