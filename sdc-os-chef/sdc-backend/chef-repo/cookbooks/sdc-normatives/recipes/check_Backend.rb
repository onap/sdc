ruby_block "check_Backend_Health" do
    block do
      printf("\033[32m%s\n\033[0m", "  executing BackEnd health-check, please wait...")
      Chef::Resource::RubyBlock.send(:include, Chef::Mixin::ShellOut)
      curl_command = "http://localhost:8080/sdc2/rest/v1/user/jh0003"
      resp = Net::HTTP.get_response URI.parse(curl_command)
      stat = resp.code

      case stat
         when '200'
            printf("\033[32m%s\n\033[0m", "  BackEnd is up.")
         else
            printf("\033[31mstat=[%s]\n\033[0m", stat)
            printf("\033[31m%s\n\033[0m", "  BackEnd is DOWN!!!")
      end
   end
   retries 12
   retry_delay 5
end
