package com.nadaproject.connections.controller;

import com.nadaproject.connections.model.ErrorResponse;
import com.nadaproject.connections.model.PathResponse;
import com.nadaproject.connections.model.Person;
import com.nadaproject.connections.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/people")
    public ResponseEntity<Person> createPerson(@RequestBody @Valid Person person) {
        Person created = connectionService.createPerson(person.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/knows")
    public ResponseEntity<Void> createConnection(@RequestBody Map<String, Long> request) {
        Long a = request.get("a");
        Long b = request.get("b");
        if (a == null || b == null) {
            throw new IllegalArgumentException("Fields 'a' and 'b' are required.");
        }
        if (a.equals(b)) {
            throw new IllegalArgumentException("Parameters 'a' and 'b' cannot be equal.");
        }
        connectionService.createConnection(a, b);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/people/{id}/contacts")
    public ResponseEntity<List<Person>> getContacts(@PathVariable Long id) {
        List<Person> contacts = connectionService.getContacts(id);
        return ResponseEntity.ok(contacts);
    }
    
    @GetMapping({"/", "/health"})
    public ResponseEntity<Map<String, String>> getHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/path")
    public ResponseEntity<PathResponse> getPath(
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Parameters 'from' and 'to' are required and must be valid numbers.");
        }
        PathResponse response = connectionService.findPath(from, to);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ConnectionService.PersonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ConnectionService.PersonNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}
