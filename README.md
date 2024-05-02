# [Distributed Systems](https://fenix.tecnico.ulisboa.pt/disciplinas/SDis15111326/2016-2017/2-semestre)

## *Introduction*

The course was taught by Professor Miguel Pardal, and this project was evaluated by Professor Naércio Magaia, who gave it a final grade of 17/20.

The project was called *Komparator*. The idea was to create an application similar to KuantoKusta, where we had a mediator module that queried various shopping server modules about the stock and price they had for a given item, and then showed this information to the client.

## *Group*

- **Francisco Pereira** (81911) @ [frankvilax](https://github.com/frankvilax)  (**Final Grade**: 13/20)
- **José Brás** (82069) @ [ist182069](https://github.com/ist182069) (**Final Grade**: 18/20)
- **Samuel Silva** (82071) @ [SamTheGolden](https://github.com/SamTheGolden) (**Final Grade**: 18/20)

## *Language and Modules Used*

This project was developed in Java. Java Web Services were used for inter-process communication. Modules like JAX-WS were also utilized. Additionally, handlers were employed in this project.

## *Directories*

- *./Enunciado* contains the project statement.
- *./ca-ws-cli* presents the client module for a certification authority for credit cards.
- *./cc-ws-cli* is the module that provides information about credit cards.
- *./mediator-ws-cli* is the client module of the mediator process. This is where communication starts, and despite its name, this is the original client module where requests are made.
- *./mediator-ws* is the server module of the mediator, which communicates with the suppliers.
- *./security* houses the security module where handlers, cipher classes, and cryptography utilities are located.
- *./supplier-ws-cli* is the client module of the supplier, meaning it's the module contacted by *./mediator-ws*.
- *./supplier-ws* is the server module of the supplier. This is where information regarding product stocks is managed.
- *./T14* in this module, additional content such as keys for suppliers and mediators, and their respective Java Key Stores, can be found.

## *Communication between Modules*

The basic communication between modules works as follows:

**mediator-ws-cli** -> **mediator-ws** -> **supplier-ws-cli** -> **supplier-ws*
