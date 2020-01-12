package com.hyit.crud.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hyit.crud.bean.Employee;
import com.hyit.crud.bean.Msg;
import com.hyit.crud.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工CRUD请求
 */
@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    /**
     * 单个 批量 二合一
     * 批量删除：1-2-3
     * 单个删除：1
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/emp/{id}",method = RequestMethod.DELETE)
    public Msg deleteEmp(@PathVariable("id") String ids) {
        //批量删除
        if(ids.contains("-")) {
            List<Integer> del_ids = new ArrayList<>();
            String[] str_ids = ids.split("-");
            //组装id的集合
            for(String str : str_ids) {
                del_ids.add(Integer.parseInt(str));
            }
            employeeService.deleteBatch(del_ids);
        } else {
            employeeService.deleteEmp(Integer.parseInt(ids));
        }
        return Msg.success();
    }

    /**
     * 如果直接发送ajax=PUT形式的请求
     * 封装的数据
     * Employee
     * Employee{empId=1009, empName='null', gender='null', email='null', dId=null, department=null}
     *
     * 问题：请求体中有数据，但是Employee对象封装不上
     *
     * 原因：
     * Tomcat：
     *      1、将请求体中的数据，封装一个map。
     *      2、request.getParameter("empName)就会从这个map中取值
     *      3、SpringMVC封装POJO对象的时候
     *          会把POJO中每个属性的值，调用request.getParameter("email");拿到
     * AJAX发送PUT请求引发的血案：
     *      PUT请求，请求体中的数据，request.getParameter("empName)拿不到
     *      Tomcat一看是PUT请求，就不会封装请求体中的数据为map，只有POST形式的请求才封装请求体为map
     *
     * 解决方案：
     * 我们要能支持直接发送PUT之类的请求，还要封装请求体中的数据
     * 必须配置上HiddenHttpMethodFilter：
     * 作用：
     *      将请求体中的数据解析包装成一个map。
     *      request被重新包装，request.parameter()被重写，就会从自己封装的map中取数据
     *
     * 员工更新方法
     * @param employee
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/emp/{empId}",method = RequestMethod.PUT)
    public Msg saveEmp(Employee employee) {
        System.out.println("将要更新的员工数据："+employee);
        employeeService.updateEmp(employee);
        return Msg.success();
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @RequestMapping(value = "/emp/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getEmp(id);
        return Msg.success().add("emp",employee);
    }

    /**
     * 要使RequestBody注解能正常使用，必须导入jackson包
     * @param pn
     * @return
     */
    @RequestMapping("/employees")
    @ResponseBody
    public Msg getEmployeesWithJson(@RequestParam(value = "pn",defaultValue = "1") Integer pn) {
        //引入PageHelper分页插件
        //在查询之前只需要调用startPage方法，插入页码以及每页显示数量
        PageHelper.startPage(pn,5);
        //startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> lists = employeeService.getAll();

        //使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了
        //封装了详细的分页信息，包括有我们查询出来的数据，可以传入连续显示的页数(5)
        PageInfo<Employee> page = new PageInfo(lists,5);
        return Msg.success().add("pageInfo",page);
    }

    /**
     * 员工保存
     * 1、支持JSR303校验
     * 2、导入Hibernate-Validator
     *
     * @return
     */
    @RequestMapping(value = "/emp",method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            //校验失败，应该返回失败，在模态框中显示校验失败的错误信息
            Map<String,Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for(FieldError fieldError : errors) {
                System.out.println("错误的字段名：" + fieldError.getField());
                System.out.println("错误信息：" + fieldError.getDefaultMessage());
                map.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields",map);
        } else {
            employeeService.saveEmp(employee);
            return Msg.success();
        }
    }

    /**
     * 检查用户名是否可用
     * @param empName
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkUser")
    public Msg checkUser(@RequestParam("empName") String empName) {
        //先判断用户名是否是合法的表达式
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\\u2E80-\\u9FFF]{2,5})";
        if(!empName.matches(regx)) {
            return Msg.fail().add("va_msg","用户名必须是6-16位英文和数组的组合，或者2-5位中文");
        }
        //数据库用户名重复校验
        boolean b = employeeService.checkUser(empName);
        if(b) {
            return Msg.success();
        } else {
            return Msg.fail().add("vc_msg","用户名不可用");
        }
    }

    /**
     * 查询员工数据（分页查询）
     * @return
     */
    //@RequestMapping("/employees")
    public String getEmployees(@RequestParam(value = "pn",defaultValue = "1") Integer pn,
                               Model model) {
        //这不是一个分页查询
        //引入PageHelper分页插件
        //在查询之前只需要调用startPage方法，插入页码以及每页显示数量
        PageHelper.startPage(pn,5);
        //startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> lists = employeeService.getAll();

        //使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了
        //封装了详细的分页信息，包括有我们查询出来的数据，可以传入连续显示的页数(5)
        PageInfo<Employee> page = new PageInfo(lists,5);
        model.addAttribute("pageInfo",page);

        return "list";
    }

}
