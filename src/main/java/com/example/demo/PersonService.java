package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Service
public class PersonService {
    private final BiFunction<PersonRepository, Person, Mono<Person>> validateBeforeInsert
            = (repo, person) -> repo.findByName(person.getName());
    @Autowired
    private PersonRepository repository;

    public Flux<Person> listAll() {
        return repository.findAll();
    }

    public Mono<Void> insert(Mono<Person> personMono) {
        return personMono
                .flatMap(person -> validateBeforeInsert.apply(repository, person))
                .switchIfEmpty(Mono.defer(() -> personMono.doOnNext(repository::save)))
                .then();
    }

    public Mono<Person> findById(String id){
        return repository.findById(id);
    }

    public Mono<Void> update(Mono<Person> personMono){
        return personMono
                .flatMap(person -> validateBeforeInsert.apply(repository, person))
                .flatMap(person -> repository.save(personMono.block()))
                .switchIfEmpty(Mono.empty())
                .then();
    }

    public Mono<Void> delete(String id){
        return repository.deleteById(id);
    }
}
