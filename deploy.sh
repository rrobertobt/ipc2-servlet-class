#!/bin/bash
set -e

CATALINA_HOME=~/Downloads/apache-tomcat-11.0.18
WAR_NAME=ejemploservlets-1.0-SNAPSHOT.war

BLUE='\033[0;34m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${BLUE}→ Compilando...${NC}"
mvn clean package -q

echo -e "${BLUE}→ Deteniendo Tomcat (si está corriendo)...${NC}"
if "$CATALINA_HOME/bin/shutdown.sh" 2>/dev/null; then
  sleep 2
fi

echo -e "${BLUE}→ Desplegando $WAR_NAME...${NC}"
cp "target/$WAR_NAME" "$CATALINA_HOME/webapps/"

echo -e "${BLUE}→ Iniciando Tomcat...${NC}"
"$CATALINA_HOME/bin/startup.sh"

echo -e "${GREEN}✓ Listo — http://localhost:8080/$WAR_NAME${NC}"
