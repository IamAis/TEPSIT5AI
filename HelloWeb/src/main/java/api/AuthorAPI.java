package api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import Entita.Author;

@Path("books/{book_id}/authors")
public class AuthorAPI {
	@GET
	public List list(@PathParam("{book_id}") long bookId) {
		Author author = new Author();
		author.setId(1);
		author.setName("Joanne");
		author.setSurname("Rowling");
		List authors = new ArrayList();
		authors.add(author);
		return authors;
	}
}
