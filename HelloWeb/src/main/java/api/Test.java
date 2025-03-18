package api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("test")
public class Test {
	@GET
	@Path("{name}")
	public String test(@PathParam("name") String name) {
		String StringMixed = "Ciao" + " " + name.toUpperCase();
		return "Brachini API: " + StringMixed;
	}
}