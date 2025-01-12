# CatchyLabs
Catchylabs banka uygulaması

İçindekiler

1.Gereksinimler

2.Kurulum

3.Proje Yapısı

4.Raporlama

5.Desteklenen Tarayıcılar


Gereksinimler

Bu projeyi çalıştırmak için aşağıdaki araç ve yazılımların sisteminizde kurulu olması gerekir:

Java Development Kit (JDK): Java 8 veya daha yeni bir sürüm

Maven: Build ve bağımlılık yönetimi için

Gauge: Test framework

Chrome veya Firefox: Tarayıcı testleri için

Selenium WebDriver: Web otomasyonu için

Not: Maven ve Gauge kurulumu sırasında PATH değişkenlerinin doğru şekilde ayarlandığından emin olun.


Kurulum

1. Gauge Kurulumu

Gauge framework'ünü kurmak için terminal veya komut istemcisine aşağıdaki komutu çalıştırın:

brew install gauge         # MacOS için
choco install gauge        # Windows için
sudo apt-get install gauge # Linux için

Kurulumun doğru yapıldığını doğrulamak için şu komutu çalıştırabilirsiniz:

gauge -v

2. Proje Şablonunun Oluşturulması

Yeni bir Gauge projesi oluşturmak için aşağıdaki komutları çalıştırın:

gauge init java

3. Bağımlılıkların Yüklenmesi

Proje dizinine giderek Maven bağımlılıklarını yükleyin:

mvn clean install

4. Selenium WebDriver Bağımlılığı Eklenmesi

pom.xml dosyasına aşağıdaki bağımlılığı ekleyin:

<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.10.0</version>
</dependency>

Maven'i yeniden çalıştırarak bağımlılıkları indirin:

mvn install

Proje Yapısı

Proje yapısı şu şekilde düzenlenmiştir:

project-root
|-- specs/                 # Test senaryolarının bulunduğu dosya
|   |-- example.spec       # Örnek bir test senaryosu
|
|-- src/test/java/         # Step implementasyonlarının bulunduğu dizin
|   |-- StepImplementation.java
|
|-- pom.xml                # Maven bağımlılıkları
|-- env/default.properties # Çevresel değişkenler

Önemli Dosyalar

specs/: Gauge spesifikasyon dosyalarını içerir. Burada test senaryolarınızı yazabilirsiniz.

StepImplementation.java: Test adımlarının Java kodlarıyla eşlendiği dosya.

default.properties: Test ortamı için yapılandırma dosyası.

