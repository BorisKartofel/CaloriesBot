# Используем образ с JDK 21 для Amazon Linux 2
FROM amazonlinux:2

ARG version=21.0.2.14-1

# Установка Amazon Corretto JDK 21 и fontconfig
RUN set -eux \
    && export GNUPGHOME="$(mktemp -d)" \
    && curl -fL -o corretto.key https://yum.corretto.aws/corretto.key \
    && gpg --batch --import corretto.key \
    && gpg --batch --export --armor '6DC3636DAE534049C8B94623A122542AB04F24E3' > corretto.key \
    && rpm --import corretto.key \
    && rm -r "$GNUPGHOME" corretto.key \
    && curl -fL -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo \
    && grep -q '^gpgcheck=1' /etc/yum.repos.d/corretto.repo \
    && echo "priority=9" >> /etc/yum.repos.d/corretto.repo \
    && yum install -y java-21-amazon-corretto-devel-$version \
    && (find /usr/lib/jvm/java-21-amazon-corretto -name src.zip -delete || true) \
    && yum install -y fontconfig \
    && yum clean all

ENV LANG C.UTF-8
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto

# Установка Maven 3.2.5
RUN set -eux \
    && yum install -y tar gzip \
    && curl -fSL -o /tmp/apache-maven.tar.gz "https://archive.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz" \
    && mkdir -p /usr/share/maven \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Создаем директорию /app внутри контейнера
RUN mkdir /app

# Устанавливаем директорию /app в качестве текущей
WORKDIR /app

# Копируем исходный код проекта внутрь контейнера
COPY . /app

# Install the dependencies in pom.xml
RUN mvn clean install

# Run Spring app
CMD mvn spring-boot:run