package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ponto de entrada da aplicação.
// @SpringBootApplication já agrupa @Configuration, @EnableAutoConfiguration e @ComponentScan.
// Como essa classe está no pacote raiz, o component scan acha tudo que está abaixo
// (controllers, services, repositories) sem precisar de configuração extra.
@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        // SpringApplication.run faz tudo: cria o contexto, roda autoconfiguração,
        // resolve as injeções de dependência e sobe o Tomcat embutido na porta 8080.
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
