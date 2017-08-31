http_request 'create-DMaaP-apiKeys' do
  action :post
  url 'http://23.253.97.75:3904/apiKeys/create'
  message ({:some => 'data'}.to_json)
  headers({
    'Content-Type' => 'application/json'
  })
end


selfEnviroment = node.chef_environment

ruby_block "create-DMaaP-apiKeys" do
    block do
      sleep(15)
      #tricky way to load this Chef::Mixin::ShellOut utilities
      Chef::Resource::RubyBlock.send(:include, Chef::Mixin::ShellOut)
      curl_command = "https://es_admin:Aa123456@#{application_host}:9200/_cluster/health?pretty=true --insecure"
      resp = Net::HTTP.get_response URI.parse(curl_command)
      stat = JSON.parse(resp.read_body)['status']

      case stat
         when "green"
            printf("\033[32m%s\n\033[0m", "  ElasticSearch tests completed successfully.")
         when "yellow"
            printf("\033[33m%s\n\033[0m", "  ElasticSearch tests completed successfully, with warnings")
         when "red"
            printf("\033[31m%s\n\033[0m", "  ElasticSearch tests failed!!!")
      end
   end
end

curl  POST -d '{"email":"Grinberg.Moti","description":"New Api Key for ASDC OS"}' http://23.253.97.75:3904/apiKeys/create

