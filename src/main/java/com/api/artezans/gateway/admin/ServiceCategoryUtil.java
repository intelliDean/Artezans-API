package com.api.artezans.gateway.admin;

public class ServiceCategoryUtil {

    public final static String CAT_NAME_DESC = "Admin adds service category to the list of existing categories by " +
            "supplying a list of category names. This will bw fetched and used to create the category dropdown" +
            "when a service provider wants to create listing";
    public final static String CAT_NAME_SUM = "Add Category Name";
    public final static String CAT_NAME_OP_ID = "category.name";
    public final static String ALL_CAT_NAME_SUM = "Get all Categories Names";
    public final static String ALL_CAT_NAME_DESC = "To display all the categories for the service provider" +
            " to select from, this endpoint is called";
    public final static String ALL_CAT_NAME_OP_ID = "all.category.name";
    public final static String SERV_CAT_NAME_SUM = "Add service category";
    public final static String SERV_CAT_NAME_DESC = "Admin adds services to their respective categories by providing " +
            "the category name and the list of services. These services will be used to create a " +
            "dropdown when a user chooses a particular category";
    public final static String SERV_CAT_NAME_OP_ID = "service.category";
    public final static String BY_CAT_NAME_SUM = "Get all services by category name";
    public final static String BY_CAT_NAME_DESC = "To view all the services under a category, this endpoint is called with the category name";
    public final static String BY_CAT_NAME_OP_ID = "service.byCatName";
    public final static String DEL_CAT_NAME_SUM = "Delete category by category name";
    public final static String DEL_CAT_NAME_DESC = "To delete a category name, this endpoint is called with the category name";
    public final static String DEL_CAT_NAME_OP_ID = "delete.byCatName";
    public final static String VIEW_ALL_CATS_SUM = "view all categories";
    public final static String VIEW_ALL_CATS_DESC = "To view all categories, this is the endpoint";
    public final static String VIEW_ALL_CATS_OP_ID = "all.categories";

}
