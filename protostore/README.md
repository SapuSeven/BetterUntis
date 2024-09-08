# ProtoStore

A library for storing typed objects with Proto DataStore and building a matching UI.

Useful for application preferences including multi-profile support.

## Installation

```groovy
dependencies {
    implementation "com.sapuseven.compose:protostore:1.0.0"
    //...
}
```

## DataStore setup

### Setup Protobuf and DataStore

Set up Protobuf to generate code in your module-level `build.gradle`:

```groovy
plugins {
	id 'com.google.protobuf' version '0.9.4'
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.0"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                java {
                    option 'lite'
                }
            }
        }
    }
}

dependencies {
    implementation "androidx.datastore:datastore:1.0.0"
    implementation "com.google.protobuf:protobuf-javalite:4.28.0"
    //...
}
```

### Define a schema

Proto DataStore requires a predefined schema in a proto file in the `app/src/main/proto/` directory.
This schema defines the type for the objects that you persist in your Proto DataStore.
To learn more about defining a proto schema, see the [protobuf language guide](https://developers.google.com/protocol-buffers/docs/proto3).

```protobuf
syntax = "proto3";

option java_package = "com.example.application";
option java_multiple_files = true;

message Settings {
  int32 example_counter = 1;
}
```

### Create a Proto DataStore

```kotlin
object SettingsSerializer : Serializer<Settings> {
  override val defaultValue: Settings = Settings.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): Settings {
    try {
      return Settings.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      throw CorruptionException("Cannot read proto.", exception)
    }
  }

  override suspend fun writeTo(
    t: Settings,
    output: OutputStream) = t.writeTo(output)
}

val Context.settingsDataStore: DataStore<Settings> by dataStore(
  fileName = "settings.pb",
  serializer = SettingsSerializer
)
```

## Building a preference screen

```kotlin
@Composable
fun Settings() {
    VerticalScrollColumn {
        Preference()
    }
}
```
