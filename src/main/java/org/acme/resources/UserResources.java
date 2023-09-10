package org.acme.resources;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import io.quarkus.security.Authenticated;
import io.undertow.server.session.Session;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.domain.User;
import org.acme.repositories.UserRepo;
import org.acme.services.UserService;

import static java.util.Objects.requireNonNull;

@Path("/")
public class UserResources {
    @Inject
    private UserRepo userRepo;
    @Inject
    private UserService userService;
    @Inject
    HttpSession httpSession;

    @Context
    UriInfo uriInfo;

    private final Template registration;
    private final Template login;

    private final Template home;

    public UserResources(Template registration, Template login, Template home) {
        this.registration = requireNonNull(registration, "registration template is required");
        this.login = requireNonNull(login, "login template is required");
        this.home = requireNonNull(home, "home template is required");
    }
    @GET
    @Path("register")

    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getRegistration(@QueryParam("name") String name) {
        return registration.instance();
    }


    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postRegistration(
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("email") String email,
            @FormParam("password") String password
    ) {
        if(userRepo.findByEmail(email)==null) {
            userService.registerUser(firstName, lastName, email, password);

            return Response.seeOther(UriBuilder.fromPath("sign-in").build()).build();
        }
        else
        {
            return Response.seeOther(UriBuilder.fromPath("register").build()).build();
        }

    }
    @GET
    @PermitAll
    @Path("sign-in")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLogin() {
        return login.instance();
    }

    @POST
    @PermitAll

    @Path("sign-in")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(
            @FormParam("email") String email,
            @FormParam("password") String password
    ) {
        User user = userRepo.findByEmail(email);

        if (user != null && isValidPassword(user, password)) {
            UriBuilder builder = uriInfo.getBaseUriBuilder();
            builder.path("home");
            httpSession.setAttribute("isLoggedIn", true);

            builder.queryParam("username", user.getFirstName());

            return Response.seeOther(builder.build()).build();

        } else {
            // Failed login
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
    }
    @GET
    @Path("home")
    @Authenticated
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getHome(@QueryParam("username") String name) {
        if(httpSession.getAttribute("isLoggedIn")!=null) {
            Boolean isLoggedIn = (Boolean) httpSession.getAttribute("isLoggedIn");
            if (isLoggedIn)
                return home.instance().data("username", name);
            else
                return login.instance();
        }
        else
            return login.instance();
    }
    @GET
    @Path("logout")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLogout() {
        httpSession.setAttribute("isLoggedIn", false);
            return login.instance();
    }

    private boolean isValidPassword(User user, String password) {
        Log.info(password);
        Log.info(user.getEncryptedPassword());
        return BcryptUtil.matches(password,user.getEncryptedPassword());
    }

}
