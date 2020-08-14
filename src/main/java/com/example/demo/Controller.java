package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class Controller {

    @Autowired
    private DataTransfer dataTransfer;

    @Autowired
    private ItemCategory itemCategory;

    @RequestMapping("/")
    public Object execute(){
//        dataTransfer.insert(new ArrayList(dataTransfer.categoriesMapper().values()));


        return dataTransfer.categoriesMapper().size();
    }

}
