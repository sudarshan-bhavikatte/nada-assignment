package com.nadaproject.connections.service;

import com.nadaproject.connections.model.PathResponse;
import com.nadaproject.connections.model.Person;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ConnectionService {
    private final Map<Long, Person> people = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> adjacencyList = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public Person createPerson(String name) {
        long id = idCounter.incrementAndGet();
        Person person = new Person(id, name);
        people.put(id, person);
        adjacencyList.put(id, ConcurrentHashMap.newKeySet());
        return person;
    }

    public void createConnection(Long a, Long b) {
        validatePersonExists(a);
        validatePersonExists(b);
        if (a.equals(b)) {
            throw new IllegalArgumentException("Cannot connect a person to themselves.");
        }
        adjacencyList.get(a).add(b);
        adjacencyList.get(b).add(a);
    }

    public List<Person> getContacts(Long id) {
        validatePersonExists(id);
        Set<Long> contactIds = adjacencyList.get(id);
        List<Person> contacts = new ArrayList<>();
        if (contactIds != null) {
            for (Long contactId : contactIds) {
                contacts.add(people.get(contactId));
            }
        }
        return contacts;
    }

    public PathResponse findPath(Long from, Long to) {
        validatePersonExists(from);
        validatePersonExists(to);

        if (from.equals(to)) {
            return new PathResponse(true, 0, List.of(people.get(from)));
        }

        Queue<Long> queue = new LinkedList<>();
        Map<Long, Long> parentMap = new HashMap<>();
        Map<Long, Integer> distanceMap = new HashMap<>();

        queue.add(from);
        distanceMap.put(from, 0);

        boolean found = false;

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            int currentDist = distanceMap.get(current);

            if (current.equals(to)) {
                found = true;
                break;
            }

            if (currentDist >= 3) {
                continue; 
            }

            Set<Long> neighbors = adjacencyList.get(current);
            if (neighbors != null) {
                for (Long neighbor : neighbors) {
                    if (!distanceMap.containsKey(neighbor)) {
                        distanceMap.put(neighbor, currentDist + 1);
                        parentMap.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (!found || distanceMap.get(to) > 3) {
            return new PathResponse(false);
        }

        List<Person> path = new ArrayList<>();
        Long curr = to;
        while (curr != null) {
            path.add(0, people.get(curr));
            curr = parentMap.get(curr);
        }

        int hops = path.size() - 1;
        return new PathResponse(true, hops, path);
    }

    public void validatePersonExists(Long id) {
        if (id == null || !people.containsKey(id)) {
            throw new PersonNotFoundException("no person " + id);
        }
    }

    public static class PersonNotFoundException extends RuntimeException {
        public PersonNotFoundException(String message) {
            super(message);
        }
    }
}