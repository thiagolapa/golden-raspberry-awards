# Golden Raspberry Awards API

API RESTful para fornecer informações sobre os vencedores e indicados ao prêmio Framboesa de Ouro (Golden Raspberry Awards).

<p align="center">
 <img src="https://img.shields.io/static/v1?label=GitHub&message=https://github.com/thiagolapa&color=8257E5&labelColor=000000" alt="@giulianabezerra" />
 <img src="https://img.shields.io/static/v1?label=Tipo&message=Desafio&color=8257E5&labelColor=000000" alt="Desafio" />
</p>

## Tecnologias

- Java 17
- Spring Boot 3.4.9
- Gradle 8.x
- H2 Database (embutido)
- JPA/Hibernate
- Lombok

## Pré-requisitos

- JDK 17
- Gradle 8.x
- Git (opcional)

## Instalação

1. Clone o repositório:
   ```bash
   git clone [https://github.com/thiagolapa/golden-raspberry-awards.git](https://github.com/thiagolapa/golden-raspberry-awards.git)
   cd golden-raspberry-awards


2. Construção do projeto:

```bash
  ./gradlew build
```


3. Executando a Aplicação

- Para iniciar a aplicação, execute:

```bash
   ./gradlew bootRun
```

- A aplicação estará disponível em: http://localhost:8080


3.1. Acesso ao Banco de Dados H2
- URL do Console H2: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:goldenraspberryawards
- Usuário: sa
- Senha:

3.2. Executando os Testes

Testes de Integração
Para executar os testes de integração:
```bash
 ./gradlew test
```

Todos os Testes
Para executar todos os testes:
```bash
 ./gradlew check
```


# Documentação da API

- GET: [/api/movies/producers-intervals]()

Retorna os produtores com maior e menor intervalo entre duas vitórias consecutivas.

- Exemplo de Resposta:
```
{
"min": [
    {
      "producer": "Producer F",
      "interval": 1,
      "previousWin": 2020,
      "followingWin": 2021
    }
],
"max": [
    {
      "producer": "Producer E",
      "interval": 5,
      "previousWin": 2010,
      "followingWin": 2015
    }
]
}
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/com/outsera/golden_raspberry_awards/
│   │   ├── config/         # Classes de configuração
│   │   ├── controller/     # Controladores REST
│   │   ├── dto/            # Objetos de transferência de dados
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositórios JPA
│   │   └── service/        # Lógica de negócios
│   └── resources/
│       ├── application.yml # Configurações da aplicação
│       └── movielist.csv   # Dados iniciais
└── test/                   # Testes
```
