package br.com.nlw.events.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.nlw.events.model.User;

@Repository
public interface IUserRepository extends CrudRepository<User, Integer>{

	public User findByEmail(String email);
}
