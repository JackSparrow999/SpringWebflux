package com.example.reactdemo;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MonoFluxTest {

    @Test
    public void testMono(){

        Mono<String> monoString = Mono.just("JavaTechie").log();
        monoString.subscribe();

    }

}
