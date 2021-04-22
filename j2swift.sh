if [ ! -e "./lib/antlr-4.5-complete.jar" ]; then
  mkdir lib
  curl https://www.antlr.org/download/antlr-4.5-complete.jar --output lib/antlr-4.5-complete.jar
  javac -classpath .:./lib/antlr-4.5-complete.jar src/*.java
fi

function translate() {
  JAVA_DIR=$(printf '%s' "$2" | sed 's/\//\\\//g')
  SWIFT_DIR=$(printf '%s' "$3" | sed 's/\//\\\//g')
  JAVA_FILE=$1
  SWIFT_FILE=$(printf '%s' "$1" | sed 's/java$/swift/' | sed "s/$JAVA_DIR/$SWIFT_DIR/")
  mkdir -p $(dirname "$SWIFT_FILE")
  echo "$JAVA_FILE -> $SWIFT_FILE"
  java -classpath src:./lib/antlr-4.5-complete.jar J2Swift "$JAVA_FILE" > "$SWIFT_FILE"
}

if [ -d "$1" ]; then
  JAVA_FILES=$(find "$1" | grep '.java$')
  for f in $JAVA_FILES; do
    translate "$f" "$1" "$2"
  done
else
  translate "$1"
fi
