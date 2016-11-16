# serve

## Usage

```
go run serve.go -data ../data -http :8000 &
```

## Request parameters

```
timestamp     [latest|1234578...]
ops           [10|100...]
args          [random|same_get]
```

## Example request

```
http://localhost:8000/?timestamp=latest&args=random&ops=10
```
