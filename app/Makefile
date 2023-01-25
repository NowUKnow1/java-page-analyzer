.DEFAULT_GOAL := build-run

setup:
	make -C app setup

clean:
	make -C app clean

build:
	make -C app build

start:
	make -C app start

install:
	make -C app install

start-dist:
	make -C app start-dist

generate-migrations:
	make -C app generate-migrations

lint:
	make -C app lint

test:
	make -C app test

report:
	make -C app report

update-deps:
	make -C app update-deps


build-run: build run

.PHONY: build