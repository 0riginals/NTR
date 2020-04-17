package com.banque.service;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.banque.classes.Action;
import com.banque.classes.User;


@Path("/users")
public class RestService {
	
	@GET
	@Path("/hello")
	@Produces("text/plain")
	public String helloWorld() {
		return "hello world";
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserById(@PathParam("id") int id) {
		Configuration conf = new Configuration();
		conf.configure();
		conf.addClass(User.class);
		Response response = null;
		System.out.println("On se trouve ici");
		try {
			SessionFactory sessionFact = conf.buildSessionFactory();
			Session session = sessionFact.openSession();
			User u = (User) session.load(User.class, (Integer) id);			
			User user = new User(u.getId(), u.getFirstName(), u.getLastName(), u.getBalance());
			System.out.println("---------------------- Debug  ---------------------");
			System.out.println(user.getFirstName());
			session.close();
			sessionFact.close();
			response = Response.status(Response.Status.OK).entity(user).build();
		} catch(HibernateException e) {
			response = Response.status(Response.Status.NOT_FOUND).entity("{\"Erreur\": \"Utilisateur inconnu\"}").build();
		}
		return response;
	}
	
	@GET
	@Path("{id}/balance")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserBalance(@PathParam("id") int id) {
		Configuration conf = new Configuration();
		conf.configure();
		conf.addClass(User.class);
		Response response = null;
		
		try {
			SessionFactory sessionFact = conf.buildSessionFactory();
			Session session = sessionFact.openSession();
			User u = (User) session.load(User.class, (Integer) id);
			double balance = u.getBalance();
			session.close();
			sessionFact.close();
			String message =  "{"+ " \"balance:\": "+ balance + "}";
			response = Response.status(Response.Status.OK).entity(message).build();
		} catch (HibernateException e) {
			response = Response.status(Response.Status.NOT_FOUND).entity("{\"Erreur\": \"Utilisateur inconnu\"}").build();
		}		
		return response;
	}
	
	@GET
	@Path("/{id}/actions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserActions(@PathParam("id") int id) {
		Configuration conf = new Configuration();
		conf.configure();
	    conf.addClass(User.class);
	    conf.addClass(Action.class);
	    Response reponse = null;
	    try {
	    	SessionFactory sessionFact = conf.buildSessionFactory();
		    Session session = sessionFact.openSession();
		    TypedQuery<Action> query = session.createQuery("Select a from Action a, User u " 
		    + "where u.id='"+ id +"' and u.id = a.userId", Action.class);
		    List<Action> result = query.getResultList();
		    session.close();
			sessionFact.close();
			if(result.isEmpty()) {
				reponse = Response.status(Response.Status.NO_CONTENT).build();
			} else {
				reponse = Response.status(Response.Status.OK).entity(result).build();
			}
	    } catch (HibernateException e) {
	    	reponse = Response.status(Response.Status.NOT_FOUND).entity("{\"Erreur\": \"Utilisateur inconnu\"}").build();
		}
	    
		return reponse;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{id}/add/{amount}")
	public Response addMoney(@PathParam("id") int id, @PathParam("amount") double amount, Action action) {
		Configuration conf = new Configuration();
		Response response = null;
		conf.configure();
	    conf.addClass(User.class);
	    conf.addClass(Action.class);
	    SessionFactory sessionFact = conf.buildSessionFactory();
	    Session session = sessionFact.openSession();
	    User user = null;
	    Transaction transaction = null;
	    
	    try {
	    	// Check if the amount is positive
	    	if(amount > 0) {
	    		transaction = session.getTransaction();
	    		User u = (User) session.load(User.class, (Integer) id);
	    		user = new User(u.getId(), u.getFirstName(), u.getLastName(), u.getBalance());
	    		if(action == null) {
	    			Action newAction = new Action();
	    			session.save(newAction);
	    		} else {
	    			session.save(action);
	    		}
	    		session.merge(user);
	    		session.flush();
	    		transaction.commit();
	    		response = Response.status(Response.Status.OK).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<message>Montant ajouté</message>").build();
	    	} else {
	    		response = Response.status(Response.Status.NOT_ACCEPTABLE).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<erreur>Le montant doit être positif</erreur>").build();
	    	}
	    } catch (HibernateException e) {
	    	response = Response.status(Response.Status.NOT_FOUND).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<erreur>Client inconnu</erreur>").build();
	    } catch(Exception e) {
	    	if(transaction != null) {
	    		transaction.rollback();
	    		response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<erreur>Erreur transaction</erreur>").build();
	    	}
	    }
	    
	    session.close();
	    sessionFact.close();
	    
	    return response;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("/{id}/substract/{amount}")
	public Response substractMoney(@PathParam("id") int id, @PathParam("amount") double amount, Action action) {
		Configuration conf = new Configuration();
		Response response = null;
		conf.configure();
	    conf.addClass(User.class);
	    conf.addClass(Action.class);
	    SessionFactory sessionFact = conf.buildSessionFactory();
	    Session session = sessionFact.openSession();
	    User user = null;
	    Transaction transaction = null;
	    try {
	    	transaction = session.beginTransaction();
	    	User u = (User) session.load(User.class, (Integer) id);
	    	if(user.getBalance() >= amount) {
	    		user.setBalance(user.getBalance()-amount);
	    		if(action == null) {
	    			Action newAction = new Action();
	    			session.save(newAction);
	    		} else {
	    			session.save(action);
	    		}
	    		session.merge(user);
	    		session.flush();
	    		transaction.commit();
	    		response = Response.status(Response.Status.OK).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<message>Montant enlevé du compte</message>").build();
	    	} else {
	    		response = Response.status(Response.Status.NOT_ACCEPTABLE).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<erreur>Pas assez d'argent</erreur>").build();
	    	}	    	
	    } catch (HibernateException e) {
	    	response = Response.status(Response.Status.FORBIDDEN).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<erreur>Utilisateur inconnu</erreur>").build();
	    } catch (Exception e) {
	    	if(transaction != null)
	    		transaction.rollback();
	    	response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+"<erreur>Erreur transaction</erreur>").build();
	    }
	    
	    session.close();
	    sessionFact.close();
	    return response;
	}
	
}
