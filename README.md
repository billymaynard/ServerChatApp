# Aplicación de Chat - Servidor

## Descripción General
Este repositorio contiene el código del lado del servidor para una aplicación de chat. El servidor se encarga de manejar las conexiones de los clientes, gestionar la autenticación de usuarios (inicio de sesión y registro) y difundir mensajes a los clientes conectados.

## Componentes Clave
- `ServerChatApp`: La clase principal que inicia el servidor y escucha las conexiones entrantes de los clientes.
- `ClientHandler`: Maneja las conexiones individuales de los clientes, procesa los mensajes entrantes y implementa la lógica para el inicio de sesión, registro y difusión de mensajes.
- `ServerConfig`: Administra la configuración del servidor, como la lectura y escritura de archivos de configuración.
- `ServerDiscoveryThread`: Facilita el descubrimiento del servidor en una red, respondiendo a solicitudes de transmisión de los clientes.
- `Util`: Proporciona funciones de utilidad como la eliminación de directorios y la creación de archivos.

## Dependencias Externas
- El servidor requiere la clase `Message` de una biblioteca externa para gestionar objetos de mensajes. Asegúrate de incluir esta biblioteca en tu proyecto para que el servidor funcione correctamente.

## Configuración y Ejecución
- Compila y construye el proyecto con las dependencias.
- Ejecuta `ServerChatApp` para iniciar el servidor.

## Notas
- Asegúrate de que la biblioteca externa `Message` se haya añadido como libreria extarna en tu proyecto.
