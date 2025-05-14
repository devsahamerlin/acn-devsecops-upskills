#!/bin/bash
sudo hostnamectl set-hostname ${name}
sudo sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/g' /etc/ssh/sshd_config
sudo echo ubuntu:${password} | /usr/sbin/chpasswd
sudo apt update -y
sudo apt upgrade -y

wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc
echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
sudo apt update -y
sudo apt install temurin-17-jdk -y
/usr/bin/java --version

sudo apt install unzip -y

#### Install Docker
sudo apt-get update
sudo apt-get install docker.io -y
sudo usermod -aG docker $USER
newgrp docker
sudo chown -R $USER:docker /var/run/docker.sock
sudo chmod 755 /var/run/docker.sock
#sudo chmod 777 /var/run/docker.sock

#sudo chown root:docker /var/run/docker.sock

wget https://github.com/mozilla/geckodriver/releases/download/v0.33.0/geckodriver-v0.33.0-linux-aarch64.tar.gz
tar -xvzf geckodriver-v0.33.0-linux-aarch64.tar.gz
chmod +x geckodriver
sudo mv geckodriver /home/ubuntu/


sudo apt install tree -y

# Download and install Docker Compose v2
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.3/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

#### Install Trivy
sudo apt-get install wget apt-transport-https gnupg lsb-release -y
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | sudo tee /usr/share/keyrings/trivy.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
sudo apt-get update
sudo apt-get install trivy -y
trivy --version

sudo apt-get install ansible -y
sudo apt-get install git -y
sudo mkdir /etc/ansible/

sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8088 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8082 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8081 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8083 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT

### Install minikube and ArgoCD
sudo apt install -y curl wget apt-transport-https
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-arm64
sudo install minikube-linux-arm64 /usr/local/bin/minikube
minikube version

curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl"
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl.sha256"
echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client

minikube start --driver=docker
minikube status
minikube addons list

sudo apt update
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs
node -v

#kubectl create namespace argocd
#kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
#sleep 4m
# kubectl get nodes
# kubectl get pods -n argocd
# kubectl get svc -n argocd
#kubectl port-forward svc/argocd-server -n argocd --address 0.0.0.0 8088:443 >> /dev/null &
# kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d
