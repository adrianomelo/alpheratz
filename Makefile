
init-frontend:
	@(cd frontend; yarn install)

init: init-frontend


start-collector:
	@(cd collector; python2 -m SimpleHTTPServer 4000)

start-backend:
	@(cd backend; sbt run)

start-frontend:
	@(cd dashboard; flutter run -d web)
	#@(cd frontend; elm make src/Main2.elm --output=elm.js; python2 -m SimpleHTTPServer 8000)

start: start-frontend start-backend start-collector

