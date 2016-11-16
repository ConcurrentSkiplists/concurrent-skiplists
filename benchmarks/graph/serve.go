package main

import (
	"flag"
	"fmt"
	"html/template"
	"io/ioutil"
	"log"
	"net/http"
	"path/filepath"
	"strconv"
)

var args = struct {
	HTTP string
	Data string
}{}

func latestTimestamp() (int, error) {
	infos, err := ioutil.ReadDir(args.Data)
	if err != nil {
		return 0, err
	}

	var max int
	for _, inf := range infos {
		n, err := strconv.Atoi(inf.Name())
		if err != nil {
			log.Println("skipping " + inf.Name())
			continue
		}
		if n > max {
			max = n
		}
	}

	return max, nil
}

func handler(w http.ResponseWriter, r *http.Request) {
	var tmpl = template.Must(template.ParseFiles("index.tmpl"))
	log.Println(r.URL)

	ts := r.FormValue("timestamp")
	ops := r.FormValue("ops")
	a := r.FormValue("args")

	if a != "random" && a != "same_get" {
		http.Error(w, "args invalid", 400)
		return
	}
	if ts == "" {
		http.Error(w, "timestamp invalid", 400)
		return
	}

	if ts == "latest" {
		t, err := latestTimestamp()
		if err != nil {
			http.Error(w, err.Error(), 400)
			return
		}
		ts = strconv.Itoa(t)
	}

	path := fmt.Sprintf("/data/%s/%s/ops/%s", ts, a, ops)
	if err := tmpl.Execute(w, struct{ Path string }{path}); err != nil {
		log.Println(err)
	}
}

func main() {
	flag.StringVar(&args.HTTP, "http", "localhost:8080", "http addr to listen on")
	flag.StringVar(&args.Data, "data", "", "path to data dir")
	flag.Parse()

	if args.Data == "" {
		log.Fatalln("-data required")
	}

	http.HandleFunc("/", handler)
	http.Handle("/data/", http.StripPrefix("/data/", http.FileServer(http.Dir(filepath.Dir(args.Data)))))

	log.Println("serving on " + args.HTTP)
	log.Fatal(http.ListenAndServe(args.HTTP, nil))
}
