package com.api.artezans.gateway.listing;

public class ListingUtil {

    public final static String CREATE_DESC = "When a Service provider wants to create listing," +
            "he does that by supplying necessary information to this endpoint";
    public final static String CREATE_SUM = "Create Listing";
    public final static String CREATE_OP_ID = "create.listing";
    public final static String IMAGES_DESC = "This endpoint is to display all images attached to a listing. " +
            "Supply the id of the listing and all the images attached to the listing will be displayed";
    public final static String IMAGES_SUM = "Display Listing Images";
    public final static String IMAGES_OP_ID = "listing.images";
    public final static String ADMIN_LIST_DESC = "Only admin can access this endpoint. When admin calls this endpoint " +
            "with the particular page number he wants to see, all listings are displayed to him, " +
            "including the ones deleted by service providers";
    public final static String ADMIN_LIST_SUM = "Admin Views Listings";
    public final static String ADMIN_LIST_OP_ID = "admin.listings";
    public final static String SP_LIST_DESC = "Service provider can view all of his own listings by calling this " +
            "endpoint with a page number of the particular page he wants to see";
    public final static String SP_LIST_SUM = "Service Provider Views Listings";
    public final static String SP_LIST_OP_ID = "service.provider.listings";
    public final static String UNDELETED_DESC = "By calling this endpoint, you get all listings but excluding the deleted ones";
    public final static String UNDELETED_SUM = "All Listings not deleted";
    public final static String UNDELETED_OP_ID = "undeleted.listings";
    public final static String UPDATE_DESC = "When a service provider want to edit or update his listing, he does that by calling " +
            "this endpoint. Note: The payload for this endpoint is unique";
    public final static String UPDATE_SUM = "Update Listing";
    public final static String UPDATE_OP_ID = "update.listing";
    public final static String DELETE_DESC = "Service provider can delete his listing by calling this endpoint with the listing id";
    public final static String DELETE_SUM = "Delete Listing";
    public final static String DELETE_OP_ID = "delete.listing";
    public final static String BY_ID_DESC = "To search for a particular listing, call this endpoint with the listing id";
    public final static String BY_ID_SUM = "Search Listing by ID";
    public final static String BY_ID_OP_ID = "listing.byId";
    public final static String SERVICE_NAME_DESC = "To search for a particular listing, call this endpoint with the listing's service name";
    public final static String SERVICE_NAME_SUM = "Search Listing by Service Name";
    public final static String SERVICE_NAME_OP_ID = "listing.serviceName";
    public final static String ADMIN_BY_ID_DESC = "Admin can search a particular listing by calling this endpoint with the listing id";
    public final static String ADMIN_BY_ID_SUM = "Admin search Listing by ID";
    public final static String ADMIN_BY_ID_OP_ID = "admin.listing.byId";
    public final static String BY_LOC_DESC = "To search for all listings within a particular location," +
            " e.g street name, town, state, country";
    public final static String BY_LOC_SUM = "Search Listing by Location";
    public final static String BY_LOC_OP_ID = "listing.byLocation";
}
