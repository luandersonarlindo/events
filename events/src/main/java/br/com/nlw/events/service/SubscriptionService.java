package br.com.nlw.events.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicadorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repository.IEventRepository;
import br.com.nlw.events.repository.ISubscriptionRepository;
import br.com.nlw.events.repository.IUserRepository;

@Service
public class SubscriptionService {

	@Autowired
	private ISubscriptionRepository subscriptionRepository;

	@Autowired
	private IEventRepository eventRepository;

	@Autowired
	private IUserRepository userRepository;

	public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {

		Event evt = eventRepository.findByPrettyName(eventName);
		if (evt == null) {
			throw new EventNotFoundException("Evento " + eventName + " não existe!");
		}

		User userRec = userRepository.findByEmail(user.getEmail());
		if (userRec == null) {
			userRec = userRepository.save(user);
		}

		User indicador = null;
		if (userId != null) {
			indicador = userRepository.findById(userId).orElse(null);
			if (indicador == null) {
				throw new UserIndicadorNotFoundException("Usuario " + userId + " indicador não existe!");
			}
		}

		Subscription subs = new Subscription();
		subs.setEvent(evt);
		subs.setSubscriber(userRec);
		subs.setIndication(indicador);

		Subscription tmpSub = subscriptionRepository.findByEventAndSubscriber(evt, userRec);
		if (tmpSub != null) {
			throw new SubscriptionConflictException(
					"Já existe inscrição para o usuário " + userRec.getName() + " no evento " + evt.getTitle());
		}

		Subscription res = subscriptionRepository.save(subs);
		return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecraft.com/subscription/"
				+ res.getEvent().getPrettyName() + "/" + res.getSubscriber().getId());
	}

	public List<SubscriptionRankingItem> getCompleteRanking(String prettyName) {
		Event evt = eventRepository.findByPrettyName(prettyName);
		if (evt == null) {
			throw new EventNotFoundException("Ranking do evento " + prettyName + " não existe.");
		}
		return subscriptionRepository.generateRanking(evt.getEventId());
	}

	public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {
		List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);

		SubscriptionRankingItem item = ranking.stream().filter(i -> i.userId().equals(userId)).findFirst().orElse(null);
		if (item == null) {
			throw new UserIndicadorNotFoundException("Não há inscrições com com indicação do usuario " + userId);
		}
		Integer posicao = IntStream.range(0, ranking.size()).filter(pos -> ranking.get(pos).userId().equals(userId))
				.findFirst().getAsInt();

		return new SubscriptionRankingByUser(item, posicao+1);
	}
}
