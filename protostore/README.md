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

For an example implementation, see below.

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

### Define a settings model and repository

#### Single-user settings

TODO

#### Multi-user settings

For multi-user preferences, you need a schema that consists of two objects:
1. A holder for settings per use
2. A global object with a map to the user-specific settings

```protobuf
syntax = "proto3";

option java_package = "com.example.application";
option java_multiple_files = true;

message UserSettings {
  bool exampleValue = 1;
}

message Settings {
  int64 activeUser = 1;
  map<int64, UserSettings> users = 2;
}
```

Then you can define a ViewModel extending `MultiUserSettingsRepository` with your defined types:

```kotlin
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
	dataStore: DataStore<Settings>
) : MultiUserSettingsRepository<Settings, Settings.Builder, UserSettings, UserSettings.Builder>(dataStore) {
	private val userId = -1;// get your active user id here

	override fun getUserSettings(dataStore: Settings) : UserSettings {
		return dataStore.usersMap.getOrDefault(userId, UserSettings.getDefaultInstance())
	}

	override fun updateUserSettings(currentData : Settings, userSettings: UserSettings) : Settings {
		return currentData.toBuilder()
			.putUsers(userId, userSettings)
			.build()
	}
}
```

## Building a settings screen

Once your model is defined, you can use the composables from the `com.sapuseven.protostore.ui` package
to build your settings screen:

```kotlin
@Composable
fun Settings() {
    VerticalScrollColumn {
        SwitchPreference(
            title = { Text("Example switch") },
            settingsRepository = viewModel,
            value = { it.exampleValue },
            onCheckedChange = { exampleValue = it }
        )
    }
}
```
