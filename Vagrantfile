fail unless Vagrant.has_plugin?('vagrant-berkshelf')
fail unless Vagrant.has_plugin?('vagrant-omnibus')
fail unless Vagrant.has_plugin?('vagrant-cachier')
Vagrant.configure('2') do |config|
  config.berkshelf.enabled = true
  config.omnibus.chef_version = :latest
  config.cache.scope = :box

  config.vm.box = ENV.fetch('VAGRANT_VM_BOX', 'opscode-centos-6.6')
  config.vm.box_url = ENV.fetch('VAGRANT_VM_BOX_URL', 'http://opscode-vm-bento.s3.amazonaws.com/vagrant/virtualbox/opscode_centos-6.6_chef-provisionerless.box')

  config.vm.define :chef_server do |guest|
    guest.vm.network :forwarded_port, guest: 443, host: 8443
    guest.vm.provision :chef_solo do |chef|
      chef.nodes_path = File.expand_path('../.vagrant/nodes', __FILE__)
      chef.run_list = %w(chef-server::standalone)
    end
  end
end
