package shared.dto;

public class OKResponseDTO extends ResponseDTO {

	public OKResponseDTO(String[] tokens) {
		this();
	}

	public OKResponseDTO() {
		super(ResponseType.OK);
	}

	@Override
	public String serialize() {
		return OKResponseDTO.class.getSimpleName();
	}

}
