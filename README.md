# Sistemas Distribuídos

## Nota Introdutória

O responsável pela cadeira foi o Professor Miguel Pardal e este projecto foi avaliado pelo Professor Naércio Magaia tendo-lhe sido atribuída a nota final de para o projecto de 17.

Este projecto foi o *Komparator*. A ideia era fazer uma aplicação semelhante ao KuantoKusta. Na qual tinhamos um módulo mediador que perguntava a vários módulos servidores de compras qual o stock e preço que tinham para um dado item mostrando depois esta informação ao cliente.

## Línguagem e Módulos Utilizados

Este projecto foi desenvolvido em Java. Para a comunicação entre processos foram utilizados Java Web Services. Foram também utilizados módulos como o JAX-WS. Este projecto utilizou também handlers.

## Directórios

- *./Enunciado* contém o enunciado do projecto.

- *./ca-ws-cli* apresenta o módulo cliente de uma autoridade de certificação para os cartões de crédito.

- *./cc-ws-cli* é o módulo que disponbiliza a informação dos cartões de crédito.

- *./mediator-ws-cli* é o módulo de cliente do processo mediator. É aqui que começa a comunicação, e apesar do nome, este é o módulo cliente original onde são feitos os pedidos

- *./mediator-ws* é o módulo de servidor do mediador, que por sua vez comunica com os suppliers.

- *./security* aqui encontra-se o módulo de segurança onde se encontram os handlers, as classes de cifra e também utilidades de criptografia.

- *./supplier-ws-cli* é o módulo de cliente do supplier, ou seja, é o módulo que é contactado pelo *./mediadtor-ws*.

- *./supplier-ws* é o módulo de servidor do supplier. É aqui que são feitas as informações que dizem respeito aos stocks dos produtos.

- *./T14* neste módulo encontra-se conteúdo adicional como chaves para os suppliers e mediadores, e as respectivas Java Key Stores.

## Comunicação entre módulos

A comunicação básica entre módulos funciona da seguinte forma.

**mediador-ws-cli** -> **mediador-ws** -> **supplier-ws-cli** -> **supplier-ws**

## Grupo T14
- 81911 - Francisco Pereira - LEIC-T (Nota final: 13)
- 82069 - José Brás - LEIC-T (Nota final: 18)
- 82071 - Samuel Silva - LEIC-T (Nota final: 18)
