# Infinite Grid

InfiniteGrid is a Grid like component that allows indefinite scrolling in both horizontal and vertical directions.
Data for cells are fetched lazily on demand from server. InfiniteGrid is not extending or using Vaadin Grid
and does not share features like resizable columns or row selection. It is aimed to provide a way to have arbitrary
number of columns without a performance penalty.

## Development instructions

Starting the test/demo server:
```
mvn jetty:run
```

This deploys demo at http://localhost:8080


