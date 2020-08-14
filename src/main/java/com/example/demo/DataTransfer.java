package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class DataTransfer {
    @Autowired
    NamedParameterJdbcTemplate template;

    SimpleJdbcInsert insert;

    @PostConstruct
    public void init() {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(
                ((JdbcTemplate) template.getJdbcOperations())
        );

        SimpleJdbcInsert withTableName = simpleJdbcInsert.withTableName("category");

        insert = withTableName.usingGeneratedKeyColumns("category_id");
    }

    public void insert(List<Category> categoriesResult) {


        //depth = 1 のcategoryのみをinsert
        for (Category category : categoriesResult) {
            if (category.getDepth() == 1) {
                String sql = "INSERT INTO category(name) VALUES(:name)";
                SqlParameterSource param = new MapSqlParameterSource().addValue("name", category.getCategoryName());
                Integer generatedKey = insert.executeAndReturnKey(param).intValue();
                String closureInsert = "INSERT INTO closure_table_relations (tree_ancestor_id, tree_descendant_id,depth)  values(:generatedKey, :generatedKey,:depth)";
                SqlParameterSource closureParam = new MapSqlParameterSource().addValue("generatedKey", generatedKey).addValue("depth", category.getDepth());
                template.update(closureInsert, closureParam);
            }
        }

        for (Category category : categoriesResult) {
            if (category.getDepth() == 2) {
                String sql = "INSERT INTO category(name) VALUES(:name)";
                SqlParameterSource param = new MapSqlParameterSource().addValue("name", category.getCategoryName());
                Integer generatedKey = insert.executeAndReturnKey(param).intValue();
                String closureInsert = "INSERT INTO closure_table_relations (tree_ancestor_id, tree_descendant_id,depth)  " +
                        "values(" +
                        "(SELECT category_id " +
                        " FROM category " +
                        " LEFT OUTER JOIN closure_table_relations as ctr" +
                        " ON category_id = ctr.tree_ancestor_id " +
                        " WHERE name = :parentName " +
                        " AND ctr.depth = 1)" +
                        ", :generatedKey,:depth) ";
                SqlParameterSource closureParam = new MapSqlParameterSource().addValue("generatedKey", generatedKey).addValue("parentName", category.getParentName()).addValue("depth", category.getDepth());
                template.update(closureInsert, closureParam);
            }
        }

        for (Category category : categoriesResult) {
            if (category.getDepth() == 3) {
                SqlParameterSource param = new MapSqlParameterSource().addValue("name", category.getCategoryName());
                Integer generatedKey = insert.executeAndReturnKey(param).intValue();

                //depth = 1 の関係性をinsert
                String closureInsertDepth1 = "INSERT INTO closure_table_relations (tree_ancestor_id, tree_descendant_id,depth) " +
                        " values(" +
                        "(SELECT category_id " +
                        " FROM category " +
                        " JOIN closure_table_relations as ctr " +
                        " ON ctr.tree_ancestor_id = category.category_id " +
                        " where category.name = :grandParentName" +
                        " AND ctr.depth = 1), :generatedKey,:depth)";
                SqlParameterSource closureParam1 = new MapSqlParameterSource()
                        .addValue("generatedKey", generatedKey)
                        .addValue("grandParentName", category.getGrandParentName())
                        .addValue("depth", category.getDepth());
                template.update(closureInsertDepth1, closureParam1);
                //depth = 2 の関係性をinsert
                String closureInsertDepth2 = "INSERT INTO closure_table_relations (tree_ancestor_id, tree_descendant_id,depth) " +
                        " values( " +
                        "(SELECT c1.category_id " +
                        "FROM category as c1 " +
                        "JOIN closure_table_relations as ctr " +
                        "ON c1.category_id = ctr.tree_descendant_id " +
                        "JOIN category as c2 " +
                        "ON ctr.tree_ancestor_id = c2.category_id " +
                        "WHERE c1.name = :parentName " +
                        "AND c2.name = :grandParentName " +
                        "AND ctr.depth = 2" +
                        ") " +
                        ", :generatedKey,:depth)";
                SqlParameterSource closureParam2 = new MapSqlParameterSource()
                        .addValue("generatedKey", generatedKey)
                        .addValue("parentName", category.getParentName())
                        .addValue("grandParentName", category.getGrandParentName())
                        .addValue("depth", category.getDepth());
                template.update(closureInsertDepth2, closureParam2);

            }
        }


//        Integer generatedKey = null;
//        for (Category category : categoriesResult) {
//            if (isNull(category.getParentName()) && isNull(category.getNameAll())) {
//                String insertSql = "INSERT INTO category(name) VALUES(:name)";
//                SqlParameterSource param = new MapSqlParameterSource().addValue("name", category.getCategoryName());
//                generatedKey = insert.executeAndReturnKey(param).intValue();
//
//            } else if (isNull(category.getNameAll())) {
//                String insertSql = "INSERT INTO category(name ,parent) VALUES(:name , :parent)";
//                SqlParameterSource param = new MapSqlParameterSource()
//                        .addValue("name", category.getCategoryName())
//                        .addValue("parent", generatedKey);
//                insert.executeAndReturnKey(param).intValue();
//
//            } else {
//                String insertSql = "INSERT INTO category(name ,parent,name_all) " +
//                        "VALUES(:name , " +
//                        ":parent" +
//                        ",:allName)";
//                SqlParameterSource param = new MapSqlParameterSource()
//                        .addValue("name", category.getCategoryName())
//                        .addValue("parent", generatedKey)
//                        .addValue("allName", category.getNameAll());
//                template.update(insertSql, param);
//            }
//
//        }
    }

    public Map<String, Category> categoriesMapper() {

        SqlParameterSource param = new MapSqlParameterSource();

        String sql = "SELECT DISTINCT category_name as category_name FROM original WHERE category_name is not null";

        List<String> originalCategoryNames = template.queryForList(sql, param, String.class);

        Map<String, Category> categoriesResult = new HashMap<>();


        for (String categoryName : originalCategoryNames) {

            // 「/」で区切られているcategoryを分割して、Listに格納する
            List<String> categories = new ArrayList<String>(Arrays.asList(categoryName.split("/")));

            //sizeが3つ以上のリストは3つにする
            while (categories.size() > 3) {
                System.out.println(categories.size());
                categories.remove(categories.size() - 1);
            }


            for (int i = 0; i < categories.size(); i++) {

                Category category = new Category();
                String mapKey = null;


                    //depth == 1
                if (i == 0) {
                    category.setCategoryName(categories.get(0));
                    category.setDepth(1);
                    mapKey = categories.get(0);
                    //depth == 2
                } else if (i == 1) {
                    category.setCategoryName(categories.get(1));
                    category.setParentName(categories.get(0));
                    mapKey = categories.get(0) + '/' + categories.get(1);
                    category.setDepth(2);
                    // depth == 3
                } else if (i == 2) {
                    category.setCategoryName(categories.get(2));
                    category.setParentName(categories.get(1));
                    category.setGrandParentName(categories.get(0));
                    category.setDepth(3);
                    mapKey = categories.get(1) + '/' + categories.get(1) + '/' + categories.get(2);
                }else {
                    System.out.println("------------------------------------------------------------------");
                }

            }
        }


        return categoriesResult;
    }

    private static final RowMapper<String> STRING_ROW_MAPPER = (rs, i) -> {
        return rs.getString("category_name");
    };


}
