Java Maven conversion of the original Python embeddings demo.

How to build:

  mvn package

Run (without OpenAI):

  java -jar target/embedding-1.0-SNAPSHOT.jar

Run (with OpenAI API key):

  set OPENAI_API_KEY=sk-...   (Windows)
  mvn package
  java -jar target/embedding-1.0-SNAPSHOT.jar

Notes:
- If OPENAI_API_KEY is not set, the app uses deterministic random embeddings for demo purposes.
- The program prints top-k similar tickets to stdout.
