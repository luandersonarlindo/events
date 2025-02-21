package br.com.nlw.events.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.nlw.events.model.Event;

@Repository
public interface IEventRepository extends CrudRepository<Event, Integer>{
	
	public Event findByPrettyName(String prettyName);
}
