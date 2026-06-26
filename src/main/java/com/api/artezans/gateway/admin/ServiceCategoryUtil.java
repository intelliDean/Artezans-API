package com.api.artezans.gateway.admin;

public interface ServiceCategoryUtil {

    String CAT_NAME_DESC = """
            Admin adds service category to the list of existing categories by supplying a list of category names.
            This will be fetched and used to create the category dropdown when a provider wants to create listing
            """;
    String CAT_NAME_SUM = "Add Category Name";
    String CAT_NAME_OP_ID = "category.name";

    String ALL_CAT_NAME_SUM = "Get all Categories Names";
    String ALL_CAT_NAME_DESC = """
            To display all the categories for the service provider to select from, this endpoint is called
            """;
    String ALL_CAT_NAME_OP_ID = "all.category.name";

    String SERV_CAT_NAME_SUM = "Add service category";
    String SERV_CAT_NAME_DESC = """
            Admin adds services to their respective categories by providing
            the category name and the list of services. These services will be used to create a
            dropdown when a user chooses a particular category""";
    String SERV_CAT_NAME_OP_ID = "service.category";

    String BY_CAT_NAME_SUM = "Get all services by category name";
    String BY_CAT_NAME_DESC = """
            To view all the services under a category, this endpoint is called with the category name
            """;
    String BY_CAT_NAME_OP_ID = "service.byCatName";

    String DEL_CAT_NAME_SUM = "Delete category by category name";
    String DEL_CAT_NAME_DESC = "To delete a category name, this endpoint is called with the category name";
    String DEL_CAT_NAME_OP_ID = "delete.byCatName";

    String VIEW_ALL_CATS_SUM = "view all categories";
    String VIEW_ALL_CATS_DESC = "To view all categories, this is the endpoint";
    String VIEW_ALL_CATS_OP_ID = "all.categories";
}
