
<!-- start project-info -->
<!--
project_title: OSSmsGateway
github_project: https://github.com/jmmanzano/OSSmsGateway
license: GPL
icon: /home/josemi/AndroidStudioProjects/OSSmsGateway/app/src/main/ic_launcher-playstore.png
homepage: https://github.com/jmmanzano/OSSmsGateway
license-badge: True
contributors-badge: False
lastcommit-badge: True
codefactor-badge: False
--->

<!-- end project-info -->

<!-- start badges -->

![License GPL](https://img.shields.io/badge/license-GPL-green)
![Contributors](https://img.shields.io/github/contributors-anon/jmmanzano/OSSmsGateway)
![Last commit](https://img.shields.io/github/last-commit/jmmanzano/OSSmsGateway)
<!-- end badges -->

<!-- start description -->
# OSSmsGateway
## Descripción
OpenSourceSmsGateway es una aplicación basada en [nanohttpd](https://github.com/NanoHttpd/nanohttpd) que permite crear una API REST en tu teléfono móvil para el envío de SMS.

Intenta mantener la compatibilidad con REST SMS Gateway.

Su nivel de madurez actual es muy bajo.





<!-- end description -->

<!-- start prerequisites -->



<!-- end prerequisites -->

<!-- start installing -->

## Instalación

Puedes descargar el proyecto y construir tu propio APK con Android Studio.

También puedes descargar el apk ya construido desde [aquí](https://github.com/jmmanzano/OSSmsGateway/releases)

Requiere permisos para LEER y ENVIAR SMS y para conocer el ESTADO DEL TELÉFONO.



<!-- end installing -->

<!-- start using -->
## Uso
Al mantener la compatibilidad con REST SMS Gateway puedes enviar SMS mediante métodos POST y GET:

- curl -X "POST" "http://192.168.1.51:8080/v1/sms/?phone=987654321&message=your%20message"
- curl -X "POST" "http://192.168.1.51:8080/v1/sms/" -d "phone=987654321" -d "message=your message"
- curl -X "GET" "http://192.168.1.51:8080/v1/sms/send/?phone=987654321&message=your%20message"

También permite leer SMS mediante peticiones GET:

- curl -X "GET" "http://192.168.1.51:8080/v1/sms/" (devolverá los 10 últimos)
- curl -X "GET" "http://192.168.1.51:8080/v1/sms/100" (devolverá los 100 últimos)

Se está iniciando la implementación de la lectura de convesaciones o hilos:
- curl -X "GET" "http://192.168.1.51:8080/v1/thread/"
- curl -X "GET" "http://192.168.1.51:8080/v1/thread/?offset=4&limit=20"

Y de conversaciones por ID:

- curl -X "GET" "http://192.168.1.51:8080/v1/thread/12/"

Se está implementando la parte de estatus, pero no es ni completa ni mantiene aún la compatibilidad con REST SMS Gateway



<!-- end using -->

<!-- start contributing -->



<!-- end contributing -->

<!-- start contributors -->



<!-- end contributors -->

<!-- start table-contributors -->

<table id="contributors">
	<tr id="info_avatar">
		<td id="jmmanzano" align="center">
			<a href="https://github.com/jmmanzano">
				<img src="" width="100px"/>
			</a>
		</td>
	</tr>
	<tr id="info_name">
		<td id="jmmanzano" align="center">
			<a href="https://github.com/jmmanzano">
				<strong></strong>
			</a>
		</td>
	</tr>
	<tr id="info_commit">
		<td id="jmmanzano" align="center">
			<a href="/commits?author=jmmanzano">
				<span id="role">💻</span>
			</a>
		</td>
	</tr>
</table>
<!-- end table-contributors -->
