package com.api.artezans.gateway.listing;

public interface ListingUtil {

    // Create listing
    String CREATE_SUM = "Create listing";
    String CREATE_DESC = """
            When a service provider wants to create a listing,
            they do so by supplying the necessary information to this endpoint.
            """;
    String CREATE_OP_ID = "create.listing";

    // View images
    String IMAGES_SUM = "Display listing images";
    String IMAGES_DESC = """
            Supply the ID of a listing and all images attached to that listing will be returned.
            """;
    String IMAGES_OP_ID = "listing.images";

    // Admin view all listings
    String ADMIN_LIST_SUM = "Admin views all listings";
    String ADMIN_LIST_DESC = """
            Only admin can access this endpoint. When admin calls it with a page number,
            all listings are displayed — including those deleted by service providers.
            """;
    String ADMIN_LIST_OP_ID = "admin.listings";

    // Service provider views own listings
    String SP_LIST_SUM = "Service provider views own listings";
    String SP_LIST_DESC = """
            A service provider can view all of their own listings by calling this endpoint
            with the page number of the particular page they want to see.
            """;
    String SP_LIST_OP_ID = "service.provider.listings";

    // All undeleted listings
    String UNDELETED_SUM = "All listings (excluding deleted)";
    String UNDELETED_DESC = """
            By calling this endpoint you get all active listings, 
            excluding the ones that have been deleted.""";
    String UNDELETED_OP_ID = "undeleted.listings";

    // Update listing
    String UPDATE_SUM = "Update listing";
    String UPDATE_DESC = """
            When a service provider wants to edit or update their listing, they call this endpoint.
            Note: the payload for this endpoint uses JSON Patch format — refer to the schema for details.
            """;
    String UPDATE_OP_ID = "update.listing";

    // Delete listing
    String DELETE_SUM = "Delete listing";
    String DELETE_DESC = """
            A service provider can soft-delete their
            listing by calling this endpoint with the listing ID.""";
    String DELETE_OP_ID = "delete.listing";

    // Find by ID (user)
    String BY_ID_SUM = "Find listing by ID";
    String BY_ID_DESC = "To retrieve a particular active listing, call this endpoint with the listing ID.";
    String BY_ID_OP_ID = "listing.byId";

    // Find by service name
    String SERVICE_NAME_SUM = "Find listings by service name";
    String SERVICE_NAME_DESC = "To search for listings by their service name, call this endpoint with the service name.";
    String SERVICE_NAME_OP_ID = "listing.serviceName";

    // Admin find by ID
    String ADMIN_BY_ID_SUM = "Admin finds listing by ID";
    String ADMIN_BY_ID_DESC = """
            Admin can retrieve any listing — including deleted ones — 
            by calling this endpoint with the listing ID.""";
    String ADMIN_BY_ID_OP_ID = "admin.listing.byId";

    // Find by location
    String BY_LOC_SUM = "Find listings by location";
    String BY_LOC_DESC = """
            To search for all listings within a particular location (street name, suburb, state, or country),
            call this endpoint with the service name and location.
            """;
    String BY_LOC_OP_ID = "listing.byLocation";
}
