# serve.go

Serves graphs for data in the `-data` directory.

## Usage

```
go run serve.go -data ../benchmarks/data -http :8000
```

## Request parameters

```
timestamp     [latest|<unix-timestamp>...]
ops           [10|100...]
args          [random|same_get]
```

## Example request

```
http://localhost:8000/?timestamp=latest&args=random&ops=10
```
