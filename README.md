# Order Book Monitor

![](demo.gif)

## Run

```shell
git clone https://github.com/mtereshchuk/order-book-monitor.git
cd order-book-monitor/
mvn clean compile assembly:single # builds single jar with dependencies
cd target/
java -jar order-book-monitor.jar ETHUSDT -limit 10 -interval 3 -color true # only symbol is mandatory
```

**Note:** Java 17 is required
