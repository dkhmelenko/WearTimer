# Project description
A Stopwatch/Timer for the Android Wear platform. Solutions provides 2 projects: one for the Android Wear platform and another for the classic Android platform. Application provides synchronization between the platforms.

#Known Issues

The times shown on wear and mobile are not the same because the stopwatch-implementation is based on System.upTimeMillis(), which is different for both devices. Have to find a way to synchronize the startTime between the devices.

#License

[Apache Licence 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Copyright 2015 Dmytro Khmelenko, Gabriel Schlatter

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
