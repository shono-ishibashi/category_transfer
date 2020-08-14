package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

@Repository
public class ItemCategory {


    @Autowired
    private NamedParameterJdbcTemplate template;

    private static final RowMapper<CategoryTransfer> CATEGORY_TRANSFER_ROW_MAPPER = (rs, i) -> {
        CategoryTransfer categoryTransfer = new CategoryTransfer();

        categoryTransfer.setId(rs.getInt("id"));
        categoryTransfer.setOriginalCategory(rs.getString("category_name"));

        return categoryTransfer;
    };

    public void category() {

        String originalSql = "SELECT id,category_name FROM original as o ";

        List<CategoryTransfer> originalCategories = template.query(originalSql, CATEGORY_TRANSFER_ROW_MAPPER);

        String updateSql = "UPDATE items " +
                "SET category =  " +
                "(SELECT child.category_id " +
                "FROM category as child " +
                "JOIN closure_table_relations ctr " +
                "ON ctr.tree_descendant_id = child.category_id " +
                "JOIN category as parent " +
                "ON parent.category_id = ctr.tree_ancestor_id " +
                "JOIN closure_table_relations as ctr_pg " +
                "ON parent.category_id = ctr_pg.tree_descendant_id " +
                "JOIN category as grand " +
                "ON ctr_pg.tree_ancestor_id = grand.category_id " +
                "where ctr.depth = 3 " +
                "AND ctr_pg.depth = 2 " +
                "AND child.name = :child " +
                "AND parent.name = :parent " +
                "AND grand.name = :grand)" +
                "WHERE id = :id ";

        for(CategoryTransfer categoryTransfer : originalCategories){
            if(nonNull(categoryTransfer.getOriginalCategory())) {
                List<String> categoryList = new ArrayList<>(Arrays.asList(categoryTransfer.getOriginalCategory().split("/")));

                SqlParameterSource param = new MapSqlParameterSource()
                        .addValue("child", categoryList.get(2))
                        .addValue("parent", categoryList.get(1))
                        .addValue("grand", categoryList.get(0))
                        .addValue("id", categoryTransfer.getId());

                template.update(updateSql, param);
            }
        }
    }

}

