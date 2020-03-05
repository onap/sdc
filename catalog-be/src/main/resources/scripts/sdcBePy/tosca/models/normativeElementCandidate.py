from sdcBePy.common.helpers import check_arguments_not_none


class NormativeElementCandidate:

    def __init__(self, file_dir, url_suffix,
                 element_name, element_from_name, with_metadata=False):
        if not check_arguments_not_none(file_dir, url_suffix, element_name, element_from_name):
            raise AttributeError("The file_dir, url_suffix, element_name, element_from_name are missing")

        self.file_dir = file_dir
        self.url_suffix = url_suffix
        self.element_name = element_name
        self.element_form_name = element_from_name
        self.with_metadata = with_metadata

    def get_parameters(self):
        return self.file_dir, self.url_suffix, self.element_name, self.element_form_name, self.with_metadata
