require "jwt"

key_file = File.join(__dir__, "AuthKey_286L4D6LRL.p8")
team_id = "X5SLMU3Z9V"
client_id = "com.middleton.imagecloneai"
key_id = "286L4D6LRL"
validity_period = 180

unless File.exist?(key_file)
  abort "Error: #{key_file} not found. Place your .p8 file in the apple_auth/ folder."
end

private_key = OpenSSL::PKey::EC.new File.read(key_file)

token = JWT.encode(
  {
    iss: team_id,
    iat: Time.now.to_i,
    exp: Time.now.to_i + 86400 * validity_period,
    aud: "https://appleid.apple.com",
    sub: client_id
  },
  private_key,
  "ES256",
  { kid: key_id }
)

puts token
